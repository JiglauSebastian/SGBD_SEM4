package repository;

import domain.Cofetar;
import domain.Tort;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class DataAccessObject {

    public List<Cofetar> getAllCofetari() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Cofetar", Cofetar.class).list();
        }
    }

    public List<Tort> getTorturiByCofetar(int cofetarId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Tort t WHERE t.cofetar.id = :id", Tort.class)
                    .setParameter("id", cofetarId)
                    .list();
        }
    }

    public void insertTort(String denumire, double pret, int cofetarId, int clientId) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Cofetar cofetar = session.get(Cofetar.class, cofetarId);
            if (cofetar != null) {
                Tort tort = new Tort(0, denumire, pret, cofetar, clientId);
                session.persist(tort);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw e;
        }
    }

    public void updateTort(int id, String denumire, double pret) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Tort tort = session.get(Tort.class, id);
            if (tort != null) {
                tort.setDenumire(denumire);
                tort.setPret(pret);
                session.merge(tort);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw e;
        }
    }

    public void deleteTort(int id) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Tort tort = session.get(Tort.class, id);
            if (tort != null) {
                session.remove(tort);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw e;
        }
    }
}