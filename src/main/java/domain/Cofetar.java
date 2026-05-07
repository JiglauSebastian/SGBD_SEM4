package domain;

public class Cofetar {
    private int id;
    private String nume;
    private String specializare;

    public Cofetar(int id, String nume, String specializare) {
        this.id = id;
        this.nume = nume;
        this.specializare = specializare;
    }

    public int getId() { return id; }
    public String getNume() { return nume; }
    public String getSpecializare() { return specializare; }
}