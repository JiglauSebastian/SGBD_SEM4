package service;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import repository.DatabaseManager;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ConnectionPoolDemoService {

    private HikariDataSource dataSource;

    public ConnectionPoolDemoService() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(DatabaseManager.getProperty("db.url"));
        config.setUsername(DatabaseManager.getProperty("db.user"));
        config.setPassword(DatabaseManager.getProperty("db.pass"));
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(5);
        config.setConnectionTimeout(3000);
        dataSource = new HikariDataSource(config);
    }

    public void runPerformanceMeasurement() {
        System.out.println("\n========== SARCINA A: Masurare Performanta ==========");

        String url = DatabaseManager.getProperty("db.url");
        String user = DatabaseManager.getProperty("db.user");
        String pass = DatabaseManager.getProperty("db.pass");

        long startNoPool = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        long endNoPool = System.currentTimeMillis();
        long timeNoPool = endNoPool - startNoPool;

        long startPool = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            try (Connection conn = dataSource.getConnection()) {
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        long endPool = System.currentTimeMillis();
        long timePool = endPool - startPool;

        System.out.println("Timp creare 100 conexiuni FARA pooling: " + timeNoPool + " ms (Medie: " + (timeNoPool / 100.0) + " ms/conn)");
        System.out.println("Timp creare 100 conexiuni CU pooling: " + timePool + " ms (Medie: " + (timePool / 100.0) + " ms/conn)");
    }

    public void runConnectionLeakDemo() {
        System.out.println("\n========== SARCINA B: Scurgere de Conexiuni (Leak) ==========");
        List<Connection> leakedConnections = new ArrayList<>();
        try {
            for (int i = 1; i <= 12; i++) {
                System.out.print("Obtinere conexiune " + i + "... ");
                Connection conn = dataSource.getConnection();
                leakedConnections.add(conn);
                System.out.println("SUCCES");
            }
        } catch (SQLException e) {
            System.out.println("\nEROARE: Pool epuizat! Timeout la obtinerea conexiunii.");
        } finally {
            for (Connection c : leakedConnections) {
                try { if (c != null && !c.isClosed()) c.close(); } catch (SQLException ignored) {}
            }
        }
    }
}