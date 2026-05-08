public class Type {
    //Attributs
    private int notype;
    private String libelle;

    public Type(int notype, String libelle) {
        this.notype = notype;
        this.libelle = libelle;
    }

    public int getNotype() {
        return notype;
    }

    public void setNotype(int notype) {
        this.notype = notype;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    @Override
    public String toString() {
        return "Type{" + "notype=" + notype + ", libelle=" + libelle + '}';
    }
    
    
    
}

