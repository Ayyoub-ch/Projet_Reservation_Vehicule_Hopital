public class Demande {
    // Attributs
    private String datereserv;
    private int numero;
    private String datedebut;
    private Personne personne;
    private int notype;
    private Vehicule vehicule;
    private int duree;
    private String dateretoureffectif;
    private String etat;

    public Demande(String datereserv, String datedebut, Personne personne, int notype, Vehicule vehicule,
            int duree, String dateretoureffectif, String etat) {
        this.datereserv = datereserv;
        //this.numero = numero;
        this.datedebut = datedebut;
        this.personne = personne;
        this.notype = notype;
        this.vehicule = vehicule;
        this.duree = duree;
        this.dateretoureffectif = dateretoureffectif;
        this.etat = etat;
    }

    // Getters et setters
    public String getDatereserv() {
        return datereserv;
    }

    public void setDatereserv(String datereserv) {
        this.datereserv = datereserv;
    }

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }

    public String getDatedebut() {
        return datedebut;
    }

    public void setDatedebut(String datedebut) {
        this.datedebut = datedebut;
    }

    public Personne getPersonne() {
        return personne;
    }

    public void setPersonne(Personne personne) {
        this.personne = personne;
    }

    public int getNotype() {
        return notype;
    }

    public void setNotype(int notype) {
        this.notype = notype;
    }

    public Vehicule getVehicule() {
        return vehicule;
    }

    public void setVehicule(Vehicule vehicule) {
        this.vehicule = vehicule;
    }

    public int getDuree() {
        return duree;
    }

    public void setDuree(int duree) {
        this.duree = duree;
    }

    public String getDateretoureffectif() {
        return dateretoureffectif;
    }

    public void setDateretoureffectif(String dateretoureffectif) {
        this.dateretoureffectif = dateretoureffectif;
    }

    public String getEtat() {
        return etat;
    }

    public void setEtat(String etat) {
        this.etat = etat;
    }

    @Override
    public String toString() {
        return "Demande [datereserv=" + datereserv + ", numero=" + numero + ", datedebut=" + datedebut + ", personne="
                + personne + ", notype=" + notype + ", vehicule=" + vehicule + ", duree=" + duree
                + ", dateretoureffectif="
                + dateretoureffectif + ", etat=" + etat + "]";
    }

    /**
     * Méthode pour créer une nouvelle demande de réservation
     * 
     * @param db Passerelle vers la base de données
     * @return true si la demande a été créée avec succès, false sinon
     */
    public boolean faireDemande(Passerelle db) {
        try {
            // Vérifier que les données essentielles sont présentes
            if (this.datereserv == null || this.datedebut == null ||
                    this.personne == null || this.vehicule == null) {
                System.out.println("ERREUR - Données manquantes pour créer la demande");
                return false;
            }

            // Insérer la demande dans la base de données
            boolean resultat = db.insererDemande(
                    this.datereserv,
                    this.datedebut,
                    this.personne.getMatricule(),
                    this.notype,
                    this.duree,
                    this.dateretoureffectif, // date de retour effectif
                    this.etat != null ? this.etat : "En attente",
                    this.vehicule.getImmat());

            if (resultat) {
                System.out.println("✓ Demande de réservation créée avec succès");
                return true;
            } else {
                System.out.println("✗ Échec de la création de la demande");
                return false;
            }
        } catch (Exception e) {
            System.out.println("ERREUR - " + e.getMessage());
            return false;
        }
    }

}
