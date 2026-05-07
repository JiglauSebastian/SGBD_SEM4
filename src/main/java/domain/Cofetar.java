package domain;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cofetar")
public class Cofetar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "nume")
    private String nume;

    @Column(name = "specializare")
    private String specializare;

    @OneToMany(mappedBy = "cofetar", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Tort> torturi = new ArrayList<>();

    public Cofetar() {}

    public Cofetar(int id, String nume, String specializare) {
        this.id = id;
        this.nume = nume;
        this.specializare = specializare;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNume() { return nume; }
    public void setNume(String nume) { this.nume = nume; }

    public String getSpecializare() { return specializare; }
    public void setSpecializare(String specializare) { this.specializare = specializare; }

    public List<Tort> getTorturi() { return torturi; }
    public void setTorturi(List<Tort> torturi) { this.torturi = torturi; }
}