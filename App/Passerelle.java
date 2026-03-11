import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;

public class Passerelle {
    private String url = "jdbc:postgresql://localhost:5432/reservation_vehicule";
    private String user = "postgres";
    private String passwd = "m7S0$]G1O3/£";
    private java.sql.Connection conn;
    private String connectionError;

    public Passerelle() {
        try {
            this.conn = DriverManager.getConnection(url, user, passwd);
            this.connectionError = null;
        } catch (Exception e) {
            this.conn = null;
            this.connectionError = e.getMessage();
            System.out.println("ERREUR - Connexion DB impossible : " + e.getMessage());
        }
    }

    private boolean hasConnection() {
        if (conn != null) {
            return true;
        }
        String detail = (connectionError == null || connectionError.isBlank()) ? "cause inconnue" : connectionError;
        System.out.println("ERREUR - Connexion DB non initialisee : " + detail);
        return false;
    }

    public java.sql.Connection getConnection() {
        return this.conn;
    }

    public void closeConnection() {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
        } catch (SQLException e) {
        }
    }

    public boolean verifierConnexion(int matricule, String mdp) {
        if (!hasConnection()) {
            return false;
        }
        try {
            PreparedStatement stmt = conn
                    .prepareStatement("SELECT nom, prenom FROM personne WHERE matricule = ?  AND mdp = ?");
            stmt.setInt(1, matricule);
            stmt.setString(2, mdp);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                System.out.println("OK - Bonjour " + rs.getString("prenom") + " " + rs.getString("nom"));
                return true;
            } else {
                System.out.println("ERREUR - Mauvais identifiants");
                return false;
            }
        } catch (Exception e) {
            System.out.println("ERREUR - " + e.getMessage());
            return false;
        }
    }

    @Override
    public String toString() {
        try {
            if (conn != null && !conn.isClosed()) {
                return "Connexion active : " + url + " | Utilisateur : " + user + " | Statut : CONNECTÉ";
            } else {
                return "Connexion inactive : " + url + " | Utilisateur : " + user + " | Statut : FERMÉE";
            }
        } catch (SQLException e) {
            return "Erreur de connexion : " + url + " | Utilisateur : " + user + " | Erreur : " + e.getMessage();
        }
    }

    public Demande ReservationVehicule(int typeVehicule, LocalDate dateReservation, LocalDate dateDebut, LocalDate dateFin, int duree) {
        if (!hasConnection()) {
            return null;
        }
        try {
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT v.immat, v.marque, v.modele FROM vehicule v WHERE v.noType = ? AND v.immat NOT IN (SELECT d.immat FROM demande d WHERE d.notype = ? AND ((d.datedebut <= ? AND d.dateretoureffectif >= ?) OR (d.datedebut <= ? AND d.dateretoureffectif >= ?) OR (d.datedebut >= ? AND d.dateretoureffectif <= ?)))) LIMIT 1");
            stmt.setInt(1, typeVehicule);
            stmt.setInt(2, typeVehicule);
            stmt.setDate(3, java.sql.Date.valueOf(dateDebut));
            stmt.setDate(4, java.sql.Date.valueOf(dateDebut));
            stmt.setDate(5, java.sql.Date.valueOf(dateFin));
            stmt.setDate(6, java.sql.Date.valueOf(dateFin));
            stmt.setDate(7, java.sql.Date.valueOf(dateDebut));
            stmt.setDate(8, java.sql.Date.valueOf(dateFin));

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String immat = rs.getString("immat");
                String marque = rs.getString("marque");
                String modele = rs.getString("modele");
                System.out.println("OK - Véhicule trouvé : " + immat + " | " + marque + " | " + modele);
                return new Demande(dateReservation.toString(), 0, dateDebut.toString(), null, typeVehicule, immat,
                        duree, dateFin.toString(), "EN ATTENTE");
            } else {
                System.out.println("ERREUR - Aucun véhicule disponible pour les dates sélectionnées.");
                return null;
            }
        } catch (Exception e) {
            System.out.println("ERREUR - " + e.getMessage());
            return null;
        }
    }

    // Fonction crée pour récupérer le numéro du type afin de fludifier la
    // vérification de la réservation
    // Réutilisable pour afficher le numero du type dans le cas de la réservation ou
    // de la modif si besoin
    public Type recupererTypeParNumero(int numero) {
        if (!hasConnection()) {
            return null;
        }
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT noType, libelle FROM type WHERE noType = ?");
            stmt.setInt(1, numero);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Type(rs.getInt("noType"), rs.getString("libelle"));
            }
        } catch (Exception e) {
            System.out.println("ERREUR récupération Type : " + e.getMessage());
        }
        return null;
    }

    public void modifierReservation(int numero, LocalDate datereserv,
            LocalDate dateDebut, String matricule,
            int noType, String immat, int duree,
            LocalDate dateRetourEffectif, String etat) {

        if (!hasConnection()) {
            return;
        }

        try {
            PreparedStatement stmt = conn
                    .prepareStatement(
                            "UPDATE demande SET datedebut = ?, matricule = ?, notype = ?, immat = ?, duree = ?, dateretoureffectif = ?, etat = ? WHERE numero = ? AND datereserv = ?");
            stmt.setDate(1, java.sql.Date.valueOf(dateDebut));
            stmt.setString(2, matricule);
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
                System.out.println("✅ Réservation modifiée avec succès !");
            } else {
                System.out.println("❌ Aucune réservation trouvée avec ce couple numéro/date.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Fonction de Validation du Véhicule
    public boolean verifierReservation(String marque, String modele, Type unType, String immat) {
        boolean verif = false;
        if (!hasConnection()) {
            return false;
        }
        try {
            PreparedStatement stmt = conn
                    .prepareStatement(
                            "SELECT COUNT(*) FROM vehicule  WHERE marque = ?  AND modele = ? AND noType = ? AND immat = ? EXIST ");
            stmt.setString(1, marque);
            stmt.setString(2, modele);
            stmt.setInt(3, unType.getNumero());
            stmt.setString(4, immat);

            ResultSet rs = stmt.executeQuery();

            // Vérifie si la première (et seule) ligne de résultat existe ET si le compte
            // est supérieur à 0
            if (rs.next() && rs.getInt(1) > 0) {
                System.out.println(
                        "ERREUR - Validation non possible - Véhicule déjà existant - Choisissez un autre véhicule ");
                verif = true;
            } else {
                System.out.println("OK - Validation de la Réservation");
                verif = false;
            }
            return verif;
        } catch (Exception e) {
            System.out.println("ERREUR - " + e.getMessage());
            return verif;
        }
    }

    public boolean reservationExiste(int numero, LocalDate datereserv) {
        if (!hasConnection()) {
            return false;
        }
        try {
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT COUNT(*) FROM demande WHERE nodemande = ? AND datereserv = ?");
            stmt.setInt(1, numero);
            stmt.setObject(2, datereserv);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;
        } catch (Exception e) {
            System.out.println("ERREUR - " + e.getMessage());
            return false;
        }
    }
}
