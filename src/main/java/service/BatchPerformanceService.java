package service;

import repository.TransactionDAO;

import java.sql.*;

public class BatchPerformanceService {

    private final TransactionDAO dao;
    private static final int TOTAL_INSERTS = 5000;
    private static final int RUNS = 3;

    public BatchPerformanceService(TransactionDAO dao) {
        this.dao = dao;
    }

    public void runComparison() {
        System.out.println("\n========== COMPARATIE PERFORMANTA INSERARE ==========");
        System.out.printf("Inserare %d inregistrari, %d rulari per abordare%n%n", TOTAL_INSERTS, RUNS);

        long[] timesA = new long[RUNS];
        long[] timesB = new long[RUNS];
        long[] timesC = new long[RUNS];

        for (int run = 0; run < RUNS; run++) {
            System.out.println("--- Rularea " + (run + 1) + " ---");

            timesA[run] = runAutoCommit();
            cleanUp();

            timesB[run] = runBatchCommit();
            cleanUp();

            timesC[run] = runSingleTransaction();
            cleanUp();

            System.out.println();
        }

        printResults(timesA, timesB, timesC);
    }

    private long runAutoCommit() {
        System.out.print("[Abordare 1 - Auto-commit] ...");
        long start = System.currentTimeMillis();
        try (Connection conn = dao.getNewConnection()) {
            String sql = "INSERT INTO employees (name, salary, department_id) VALUES (?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                for (int i = 0; i < TOTAL_INSERTS; i++) {
                    pstmt.setString(1, "TestEmployee_A_" + i);
                    pstmt.setDouble(2, 3000);
                    pstmt.setInt(3, 1);
                    pstmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            System.out.println(" Eroare: " + e.getMessage());
            return -1;
        }
        long elapsed = System.currentTimeMillis() - start;
        System.out.println(" " + elapsed + " ms");
        return elapsed;
    }

    private long runBatchCommit() {
        System.out.print("[Abordare 2 - Commit la 100]  ...");
        long start = System.currentTimeMillis();
        try (Connection conn = dao.getNewConnection()) {
            conn.setAutoCommit(false);
            String sql = "INSERT INTO employees (name, salary, department_id) VALUES (?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                for (int i = 0; i < TOTAL_INSERTS; i++) {
                    pstmt.setString(1, "TestEmployee_B_" + i);
                    pstmt.setDouble(2, 3000);
                    pstmt.setInt(3, 1);
                    pstmt.executeUpdate();
                    if ((i + 1) % 100 == 0) {
                        conn.commit();
                    }
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.out.println(" Eroare: " + e.getMessage());
            return -1;
        }
        long elapsed = System.currentTimeMillis() - start;
        System.out.println(" " + elapsed + " ms");
        return elapsed;
    }

    private long runSingleTransaction() {
        System.out.print("[Abordare 3 - Tranzactie unica] ...");
        long start = System.currentTimeMillis();
        try (Connection conn = dao.getNewConnection()) {
            conn.setAutoCommit(false);
            String sql = "INSERT INTO employees (name, salary, department_id) VALUES (?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                for (int i = 0; i < TOTAL_INSERTS; i++) {
                    pstmt.setString(1, "TestEmployee_C_" + i);
                    pstmt.setDouble(2, 3000);
                    pstmt.setInt(3, 1);
                    pstmt.addBatch();
                    if ((i + 1) % 50 == 0) {
                        pstmt.executeBatch();
                    }
                }
                pstmt.executeBatch();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.out.println(" Eroare: " + e.getMessage());
            return -1;
        }
        long elapsed = System.currentTimeMillis() - start;
        System.out.println(" " + elapsed + " ms");
        return elapsed;
    }

    private void cleanUp() {
        try (Connection conn = dao.getNewConnection()) {
            conn.setAutoCommit(false);
            dao.deleteTestEmployees(conn);
        } catch (SQLException e) {
            System.out.println("[Cleanup] Eroare: " + e.getMessage());
        }
    }

    private void printResults(long[] a, long[] b, long[] c) {
        System.out.println("========== REZULTATE ==========");
        System.out.printf("%-35s %-10s %-10s %-10s %-10s%n",
                "Abordare", "Run 1(ms)", "Run 2(ms)", "Run 3(ms)", "Media(ms)");
        System.out.println("-".repeat(80));
        System.out.printf("%-35s %-10d %-10d %-10d %-10.0f%n",
                "1. Auto-commit (1 tx/insert)", a[0], a[1], a[2], avg(a));
        System.out.printf("%-35s %-10d %-10d %-10d %-10.0f%n",
                "2. Commit la 100 inserari", b[0], b[1], b[2], avg(b));
        System.out.printf("%-35s %-10d %-10d %-10d %-10.0f%n",
                "3. Tranzactie unica (batch)", c[0], c[1], c[2], avg(c));
        System.out.println("-".repeat(80));
        System.out.println("\nCONCLUZIE:");
        System.out.println("- Abordarea 1 (auto-commit) este cea mai lenta: o tranzactie per inserare.");
        System.out.println("- Abordarea 2 (commit la 100) reduce numarul de tranzactii de 50x.");
        System.out.println("- Abordarea 3 (batch + o singura tranzactie) este cea mai rapida:");
        System.out.println("  minimizeaza overhead-ul de retea si de commit.\n");
    }

    private double avg(long[] vals) {
        long sum = 0;
        for (long v : vals) sum += v;
        return (double) sum / vals.length;
    }
}