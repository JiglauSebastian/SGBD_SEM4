package domain;

public class Tort {
    private int id;
    private String denumire;
    private double pret;
    private int cofetarId;
    private int clientId;

    public Tort(int id, String denumire, double pret, int cofetarId, int clientId) {
        this.id = id;
        this.denumire = denumire;
        this.pret = pret;
        this.cofetarId = cofetarId;
        this.clientId = clientId;
    }

    public int getId() { return id; }
    public String getDenumire() { return denumire; }
    public double getPret() { return pret; }
    public int getCofetarId() { return cofetarId; }
    public int getClientId() { return clientId; }
}