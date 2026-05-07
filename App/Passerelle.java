import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;

public class Passerelle {

    // ── Paramètres de connexion ─────────────────────────────────────────────
    private static final String URL    = "jdbc:postgresql://localhost:5432/reservation_vehicule_sio2";
    private static final String USER   = "postgres";
    private static final String PASSWD = "m7S0$]G1O3/£";

    // ── État interne ────────────────────────────────────────────────────────
    private Connection conn = null;
    private int    matriculeConnecte = 0;
    private String roleConnecte      = null;

    // ── Connexion à la base de données ──────────────────────────────────────

    public Passerelle() {
        try {
            this.conn = DriverManager.getConnection(URL, USER, PASSWD);
            System.out.println("Connexion a la base de donnees etablie avec succes.");
        } catch (SQLException e) {
            System.err.println("ERREUR - Impossible de se connecter a la base de donnees.");
            System.err.println("  URL     : " + URL);
            System.err.println("  Message : " + e.getMessage());
        }
    }
    /* Singleton singleton = Singleton.getInstance();*/ 
    
    public boolean isConnected() {
        try {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    public Connection getConnection() {
        return this.conn;
    }

    public void closeConnection() {
        try {
            if (isConnected()) {
                conn.close();
                System.out.println("Connexion a la base de donnees fermee.");
            }
        } catch (SQLException e) {
            System.err.println("ERREUR lors de la fermeture : " + e.getMessage());
        }
    }

    // ── Authentification ────────────────────────────────────────────────────

    /**
     * Vérifie les identifiants de l'utilisateur.
     * Stocke le matricule et le rôle en majuscules si la connexion réussit.
     * @return true si l'authentification est réussie
     */
    public boolean verifierConnexion(int matricule, String mdp) {
        if (!isConnected()) {
            System.out.println("ERREUR - Pas de connexion a la base de donnees.");
            return false;
        }

        String sql = "SELECT nom, prenom, role FROM personne WHERE matricule = ? AND mdp = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, matricule);
            stmt.setString(2, mdp);

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    System.out.println("Identifiants incorrects.");
                    return false;
                }

                String role = rs.getString("role");

                this.matriculeConnecte = matricule;
                this.roleConnecte = (role != null && role.equalsIgnoreCase("role_admin"))
                        ? "Admin"
                        : "User";

                System.out.println("\nConnexion reussie !");
                System.out.println("Bienvenue " + rs.getString("prenom") + " " + rs.getString("nom"));
                System.out.println("Role : " + this.roleConnecte);
                return true;
            }
        } catch (SQLException e) {
            System.out.println("ERREUR SQL - " + e.getMessage());
            return false;
        }
    }

    // ── Accesseurs ──────────────────────────────────────────────────────────

    public int    getMatriculeConnecte() { return this.matriculeConnecte; }
    public String getRoleConnecte()      { return this.roleConnecte; }

    @Override
    public String toString() {
        return isConnected()
                ? "Connexion active  : " + URL + " | Utilisateur : " + USER
                : "Connexion inactive : " + URL + " | Utilisateur : " + USER;
    }

    // ── Requêtes metier ─────────────────────────────────────────────────────

    public Type recupererTypeParNumero(int numero) {
        if (!isConnected()) return null;

        String sql = "SELECT type.notype, type.libelle FROM type WHERE notype = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, numero);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Type(rs.getInt("notype"), rs.getString("libelle"));
                }
            }
        } catch (SQLException e) {
            System.out.println("ERREUR recuperation Type - " + e.getMessage());
        }
        return null;
    }

    public void modifierReservation(int numero, LocalDate datereserv,
            LocalDate dateDebut, int matricule,
            int noType, String immat, int duree,
            LocalDate dateRetourEffectif, String etat) {

        String sql = "UPDATE demande SET datedebut = ?, matricule = ?, notype = ?, immat = ?, "
                   + "duree = ?, dateretoureffectif = ?, etat = ? "
                   + "WHERE numero = ? AND datereserv = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, java.sql.Date.valueOf(dateDebut));
            stmt.setInt(2, matricule);
            stmt.setInt(3, noType);
            stmt.setString(4, immat);
            stmt.setInt(5, duree);
            if (dateRetourEffectif != null) {
                stmt.setDate(6, java.sql.Date.valueOf(dateRetourEffectif));
            } else {
                stmt.setNull(6, Types.DATE);
            }
            stmt.setString(7, etat);
            stmt.setInt(8, numero);
            stmt.setDate(9, java.sql.Date.valueOf(datereserv));

            int lignes = stmt.executeUpdate();
            if (lignes > 0) {
                System.out.println("Reservation modifiee avec succes !");
            } else {
                System.out.println("Aucune reservation trouvee avec ce couple numero/date.");
            }
        } catch (SQLException e) {
            System.out.println("ERREUR SQL lors de la modification - " + e.getMessage());
        }
    }

    public boolean verifierReservation(String marque, String modele, Type unType, String immat) {
        String sql = "SELECT COUNT(*) FROM vehicule "
                   + "WHERE marque = ? AND modele = ? AND vehicule.noType = ? AND immat = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, marque);
            stmt.setString(2, modele);
            stmt.setInt(3, unType.getNumero());
            stmt.setString(4, immat);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    System.out.println("Vehicule deja existant - choisissez un autre vehicule.");
                    return true;
                }
            }
        } catch (SQLException e) {
            System.out.println("ERREUR - " + e.getMessage());
        }
        return false;
    }

    public boolean reservationExiste(int numero, LocalDate datereserv) {
        String sql = "SELECT COUNT(*) FROM demande WHERE numero = ? AND datereserv = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, numero);
            stmt.setObject(2, datereserv);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.out.println("ERREUR - " + e.getMessage());
        }
        return false;
    }

    public boolean insererDemande(String datereserv, String datedebut, int matricule,
            int notype, int duree, String dateretoureffectif, String etat, String immat) {
        String sql = "INSERT INTO demande (datereserv, datedebut, matricule, notype, immat, duree, dateretoureffectif, etat) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, java.sql.Date.valueOf(datereserv));
            stmt.setDate(2, java.sql.Date.valueOf(datedebut));
            stmt.setInt(3, matricule);
            stmt.setInt(4, notype);
            stmt.setString(5, immat);
            stmt.setInt(6, duree);
            stmt.setDate(7, java.sql.Date.valueOf(dateretoureffectif));
            stmt.setString(8, etat);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("ERREUR lors de l'insertion de la demande - " + e.getMessage());
            return false;
        }
    }

    public void afficherToutesLesReservations() {
        String sql = "SELECT d.numero, d.datereserv, d.datedebut, d.duree, d.etat, "
                   + "p.nom, p.prenom, v.marque, v.modele, v.immat "
                   + "FROM demande d "
                   + "JOIN personne p ON d.matricule = p.matricule "
                   + "JOIN vehicule v ON d.immat = v.immat "
                   + "ORDER BY d.datedebut DESC";

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            System.out.println("\n=== TOUTES LES RESERVATIONS [ADMIN] ===");
            boolean trouve = false;
            while (rs.next()) {
                trouve = true;
                System.out.println("\nReservation N°" + rs.getInt("numero"));
                System.out.println("  Employe          : " + rs.getString("prenom") + " " + rs.getString("nom"));
                System.out.println("  Date reservation : " + rs.getDate("datereserv"));
                System.out.println("  Date debut       : " + rs.getDate("datedebut"));
                System.out.println("  Duree            : " + rs.getInt("duree") + " jour(s)");
                System.out.println("  Vehicule         : " + rs.getString("marque") + " "
                        + rs.getString("modele") + " (" + rs.getString("immat") + ")");
                System.out.println("  Etat             : " + rs.getString("etat"));
            }
            if (!trouve) System.out.println("Aucune reservation dans le systeme.");

        } catch (SQLException e) {
            System.out.println("ERREUR - " + e.getMessage());
        }
    }

    public void afficherTypes() {
        if (!isConnected()) {
            System.out.println("Pas de connexion a la base de donnees.");
            return;
        }
        String sql = "SELECT notype, libelle FROM type ORDER BY notype";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                System.out.println("  " + rs.getInt("notype") + " - " + rs.getString("libelle"));
            }
        } catch (SQLException e) {
            System.out.println("ERREUR - " + e.getMessage());
        }
    }

    public void afficherVehiculesParType(int noType) {
        String sql = "SELECT immat, marque, modele "
           + "FROM vehicule "
           + "WHERE vehicule.\"noType\" = ? "
           + "ORDER BY marque, modele";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, noType);
            try (ResultSet rs = stmt.executeQuery()) {
                boolean trouve = false;
                while (rs.next()) {
                    trouve = true;
                    System.out.println("  " + rs.getString("immat") + " - "
                            + rs.getString("marque") + " " + rs.getString("modele"));
                }
                if (!trouve) System.out.println("  Aucun vehicule de ce type.");
            }
        } catch (SQLException e) {
            System.out.println("ERREUR - " + e.getMessage());
        }
    }

    public Personne recupererPersonneParMatricule(int matricule) {
        String sql = "SELECT matricule, nom, telephone FROM personne WHERE matricule = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, matricule);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Personne(
                            rs.getInt("matricule"),
                            rs.getString("nom"),
                            rs.getString("telephone"));
                }
            }
        } catch (SQLException e) {
            System.out.println("ERREUR recuperation Personne - " + e.getMessage());
        }
        return null;
    }

    public Vehicule recupererVehiculeParImmat(String immat) {
        String sql = "SELECT immat, marque, modele FROM vehicule WHERE immat = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, immat);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Vehicule(
                            rs.getString("immat"),
                            rs.getString("marque"),
                            rs.getString("modele"));
                }
            }
        } catch (SQLException e) {
            System.out.println("ERREUR recuperation Vehicule - " + e.getMessage());
        }
        return null;
    }

    public void afficherReservationsEnAttente() {
        String sql = "SELECT d.numero, d.datereserv, d.datedebut, d.duree, "
                   + "p.nom, p.prenom, p.matricule, "
                   + "v.marque, v.modele, v.immat, "
                   + "t.libelle AS typeLibelle "
                   + "FROM demande d "
                   + "JOIN personne p ON d.matricule = p.matricule "
                   + "JOIN vehicule v ON d.immat = v.immat "
                   + "JOIN type t ON d.notype = t.noType "
                   + "WHERE d.etat = 'En attente' "
                   + "ORDER BY d.datereserv ASC";

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            System.out.println("\n=== RESERVATIONS EN ATTENTE ===");
            boolean trouve = false;
            while (rs.next()) {
                trouve = true;
                System.out.println("\nReservation N°" + rs.getInt("numero")
                        + " du " + rs.getDate("datereserv"));
                System.out.println("  Employe          : " + rs.getString("prenom") + " "
                        + rs.getString("nom") + " (matricule : " + rs.getString("matricule") + ")");
                System.out.println("  Date debut       : " + rs.getDate("datedebut"));
                System.out.println("  Duree            : " + rs.getInt("duree") + " jour(s)");
                System.out.println("  Type vehicule    : " + rs.getString("typeLibelle"));
                System.out.println("  Vehicule         : " + rs.getString("marque") + " "
                        + rs.getString("modele") + " (" + rs.getString("immat") + ")");
            }
            if (!trouve) System.out.println("Aucune reservation en attente.");

        } catch (SQLException e) {
            System.out.println("ERREUR - " + e.getMessage());
        }
    }

    public boolean validerReservation(int numero, LocalDate datereserv) {
        String sql = "UPDATE demande SET etat = 'Validee' "
                   + "WHERE numero = ? AND datereserv = ? AND etat = 'En attente'";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, numero);
            stmt.setDate(2, java.sql.Date.valueOf(datereserv));
            int lignes = stmt.executeUpdate();
            if (lignes > 0) {
                System.out.println("Reservation N°" + numero + " validee avec succes.");
                return true;
            } else {
                System.out.println("Impossible de valider : reservation introuvable ou deja traitee.");
                return false;
            }
        } catch (SQLException e) {
            System.out.println("ERREUR SQL lors de la validation - " + e.getMessage());
            return false;
        }
    }

    public boolean refuserReservation(int numero, LocalDate datereserv) {
        String sql = "UPDATE demande SET etat = 'Refusee' "
                   + "WHERE numero = ? AND datereserv = ? AND etat = 'En attente'";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, numero);
            stmt.setDate(2, java.sql.Date.valueOf(datereserv));
            int lignes = stmt.executeUpdate();
            if (lignes > 0) {
                System.out.println("Reservation N°" + numero + " refusee.");
                return true;
            } else {
                System.out.println("Impossible de refuser : reservation introuvable ou deja traitee.");
                return false;
            }
        } catch (SQLException e) {
            System.out.println("ERREUR SQL lors du refus - " + e.getMessage());
            return false;
        }
    }

    public void afficherMesReservations(String matricule) {
        String sql = "SELECT d.numero, d.datereserv, d.datedebut, d.duree, d.etat, "
                   + "v.marque, v.modele, v.immat "
                   + "FROM demande d "
                   + "JOIN vehicule v ON d.immat = v.immat "
                   + "WHERE d.matricule = ? "
                   + "ORDER BY d.datedebut DESC";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, matricule);
            try (ResultSet rs = stmt.executeQuery()) {
                System.out.println("\n=== VOS RESERVATIONS ===");
                boolean trouve = false;
                while (rs.next()) {
                    trouve = true;
                    System.out.println("\nReservation N°" + rs.getInt("numero"));
                    System.out.println("  Date reservation : " + rs.getDate("datereserv"));
                    System.out.println("  Date debut       : " + rs.getDate("datedebut"));
                    System.out.println("  Duree            : " + rs.getInt("duree") + " jour(s)");
                    System.out.println("  Vehicule         : " + rs.getString("marque") + " "
                            + rs.getString("modele") + " (" + rs.getString("immat") + ")");
                    System.out.println("  Etat             : " + rs.getString("etat"));
                }
                if (!trouve) System.out.println("Aucune reservation trouvee.");
            }
        } catch (SQLException e) {
            System.out.println("ERREUR - " + e.getMessage());
        }
    }
}
