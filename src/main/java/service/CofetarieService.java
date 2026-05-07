package service;

import domain.Cofetar;
import domain.Tort;
import repository.DataAccessObject;

import java.sql.SQLException;
import java.util.List;

public class CofetarieService {
    private DataAccessObject dao;

    public CofetarieService(DataAccessObject dao) {
        this.dao = dao;
    }

    public List<Cofetar> getAllCofetari() throws SQLException {
        return dao.getAllCofetari();
    }

    public List<Tort> getTorturiByCofetar(int cofetarId) throws SQLException {
        return dao.getTorturiByCofetar(cofetarId);
    }

    public void adaugaTort(String denumire, double pret, int cofetarId, int clientId) throws SQLException {
        dao.insertTort(denumire, pret, cofetarId, clientId);
    }

    public void editeazaTort(int id, String denumire, double pret) throws SQLException {
        dao.updateTort(id, denumire, pret);
    }

    public void stergeTort(int id) throws SQLException {
        dao.deleteTort(id);
    }
}