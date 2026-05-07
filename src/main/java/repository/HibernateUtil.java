package repository;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateUtil {
    private static final SessionFactory sessionFactory = buildSessionFactory();

    private static SessionFactory buildSessionFactory() {
        try {
            Configuration configuration = new Configuration();
            configuration.configure("hibernate.cfg.xml");

            String url = DatabaseManager.getProperty("db.url");
            String user = DatabaseManager.getProperty("db.user");
            String pass = DatabaseManager.getProperty("db.pass");

            // ADAUGĂM ASTA PENTRU TEST:
            if (pass == null || pass.trim().isEmpty()) {
                throw new RuntimeException("\n\n[EROARE CRITICA] Parola nu a fost citita din bd.config! Verifica daca fisierul e in src/main/resources/\n");
            }

            configuration.setProperty("hibernate.hikari.dataSource.url", url);
            configuration.setProperty("hibernate.hikari.dataSource.user", user);
            configuration.setProperty("hibernate.hikari.dataSource.password", pass);

            return configuration.buildSessionFactory();
        } catch (Throwable ex) {
            System.err.println("Initial SessionFactory creation failed." + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public static void shutdown() {
        getSessionFactory().close();
    }
}