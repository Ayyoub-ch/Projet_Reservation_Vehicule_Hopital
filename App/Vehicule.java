public class Vehicule {
    private String immat;
    private String marque;
    private String modele;
    private Type unType;

    public Vehicule(String immat, String marque, String modele) {
        this.immat = immat;
        this.marque = marque;
        this.modele = modele;
        this.unType = null;
    }

    public Vehicule(String immat, String marque, String modele, Type unType) {
        this.immat = immat;
        this.marque = marque;
        this.modele = modele;
        this.unType = unType;
    }

    public String getImmat() {
        return immat;
    }

    public String getMarque() {
        return marque;
    }

    public String getModele() {
        return modele;
    }

    public void setImmat(String immat) {
        this.immat = immat;
    }

    public void setMarque(String marque) {
        this.marque = marque;
    }

    public void setModele(String modele) {
        this.modele = modele;
    }

    public Type getUnType() {
        return unType;
    }

    public void setUnType(Type unType) {
        this.unType = unType;
    }

    @Override
    public String toString() {
        return "Vehicule{" +
                "immat='" + immat + '\'' +
                ", marque='" + marque + '\'' +
                ", modele='" + modele + '\'' +
                ", type=" + (unType != null ? unType.getLibelle() : "N/A") +
                '}';
    }
}
