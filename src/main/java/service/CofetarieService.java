package service;

import domain.Cofetar;
import domain.Tort;
import repository.DataAccessObject;
import java.util.List;

public class CofetarieService {
    private DataAccessObject dao;

    public CofetarieService(DataAccessObject dao) {
        this.dao = dao;
    }

    public List<Cofetar> getAllCofetari() {
        return dao.getAllCofetari();
    }

    public List<Tort> getTorturiByCofetar(int cofetarId) {
        return dao.getTorturiByCofetar(cofetarId);
    }

    public void adaugaTort(String denumire, double pret, int cofetarId, int clientId) {
        dao.insertTort(denumire, pret, cofetarId, clientId);
    }

    public void editeazaTort(int id, String denumire, double pret) {
        dao.updateTort(id, denumire, pret);
    }

    public void stergeTort(int id) {
        dao.deleteTort(id);
    }
}