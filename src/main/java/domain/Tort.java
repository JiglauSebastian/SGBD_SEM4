package domain;

import jakarta.persistence.*;

@Entity
@Table(name = "tort")
public class Tort {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "denumire")
    private String denumire;

    @Column(name = "pret")
    private double pret;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cofetar_id", nullable = false)
    private Cofetar cofetar;

    @Column(name = "client_id")
    private int clientId;

    public Tort() {}

    public Tort(int id, String denumire, double pret, Cofetar cofetar, int clientId) {
        this.id = id;
        this.denumire = denumire;
        this.pret = pret;
        this.cofetar = cofetar;
        this.clientId = clientId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getDenumire() { return denumire; }
    public void setDenumire(String denumire) { this.denumire = denumire; }

    public double getPret() { return pret; }
    public void setPret(double pret) { this.pret = pret; }

    public Cofetar getCofetar() { return cofetar; }
    public void setCofetar(Cofetar cofetar) { this.cofetar = cofetar; }

    public int getClientId() { return clientId; }
    public void setClientId(int clientId) { this.clientId = clientId; }
}