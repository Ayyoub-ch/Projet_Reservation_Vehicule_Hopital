public class Personne {
    // Attributs
    private int matricule;
    private String nom;
    private String telephone;
    private Service unService;

    public Personne(int matricule, String nom, String telephone) {
        this.matricule = matricule;
        this.nom = nom;
        this.telephone = telephone;
        this.unService = null;
    }

    public Personne(int matricule, String nom, String telephone, Service unService) {
        this.matricule = matricule;
        this.nom = nom;
        this.telephone = telephone;
        this.unService = unService;
    }
    // getters et setters

    @Override
    public String toString() {
        return "Personne{" + "matricule=" + matricule + ", nom=" + nom + ", telephone=" + telephone + '}';
    }

    public int getMatricule() {
        return matricule;
    }

    public void setMatricule(int matricule) {
        this.matricule = matricule;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public Service getUnService() {
        return unService;
    }

    public void setUnService(Service unService) {
        this.unService = unService;
    }
}
