package service;

import repository.TransactionDAO;

import java.sql.*;

public class DeadlockDemoService {

    private final TransactionDAO dao;

    public DeadlockDemoService(TransactionDAO dao) {
        this.dao = dao;
    }

    public void demonstrateDeadlock() throws InterruptedException {
        System.out.println("\n========== DEADLOCK DEMO ==========");
        System.out.println("Scenariu: A blocheaza randul 1, B blocheaza randul 2.");
        System.out.println("         Apoi A vrea randul 2, B vrea randul 1 => DEADLOCK.\n");

        try { dao.resetSalary(1, 5000); dao.resetSalary(2, 5000); } catch (Exception ignored) {}

        Thread transactionA = new Thread(() -> {
            try (Connection conn = dao.getNewConnection()) {
                conn.setAutoCommit(false);
                System.out.println("[A] BEGIN TRANSACTION");
                dao.updateSalary(conn, 1, 6000);
                System.out.println("[A] UPDATE salary=6000 WHERE id=1 (rand 1 blocat)");
                Thread.sleep(2000);
                System.out.println("[A] Incearca UPDATE id=2...");
                dao.updateSalary(conn, 2, 7000);
                conn.commit();
                System.out.println("[A] COMMIT reusit");
            } catch (SQLException e) {
                System.out.println("[A] *** DEADLOCK detectat! Tranzactia A a fost victima rollback-ului. ***");
                System.out.println("[A] Mesaj eroare: " + e.getMessage().split("\n")[0]);
            } catch (InterruptedException ignored) {}
        });

        Thread transactionB = new Thread(() -> {
            try (Connection conn = dao.getNewConnection()) {
                conn.setAutoCommit(false);
                System.out.println("[B] BEGIN TRANSACTION");
                dao.updateSalary(conn, 2, 6000);
                System.out.println("[B] UPDATE salary=6000 WHERE id=2 (rand 2 blocat)");
                Thread.sleep(2000);
                System.out.println("[B] Incearca UPDATE id=1...");
                dao.updateSalary(conn, 1, 7000);
                conn.commit();
                System.out.println("[B] COMMIT reusit");
            } catch (SQLException e) {
                System.out.println("[B] *** DEADLOCK detectat! Tranzactia B a fost victima rollback-ului. ***");
                System.out.println("[B] Mesaj eroare: " + e.getMessage().split("\n")[0]);
            } catch (InterruptedException ignored) {}
        });

        transactionA.start();
        Thread.sleep(300);
        transactionB.start();
        transactionA.join();
        transactionB.join();

        System.out.println("\n--- PostgreSQL detecteaza deadlock-ul automat si face rollback pe una din tranzactii. ---");
        System.out.println("--- Solutie: Acceseaza resursele intotdeauna in aceeasi ordine. ---\n");

        demonstrateDeadlockSolution();
    }

    private void demonstrateDeadlockSolution() throws InterruptedException {
        System.out.println("Ambele tranzactii acceseaza id=1 mai intai, apoi id=2.\n");

        try { dao.resetSalary(1, 5000); dao.resetSalary(2, 5000); } catch (Exception ignored) {}

        Thread transactionA = new Thread(() -> {
            try (Connection conn = dao.getNewConnection()) {
                conn.setAutoCommit(false);
                System.out.println("[A] BEGIN TRANSACTION (ordonat)");
                dao.updateSalary(conn, 1, 6000);
                System.out.println("[A] UPDATE id=1 => 6000");
                Thread.sleep(500);
                dao.updateSalary(conn, 2, 7000);
                System.out.println("[A] UPDATE id=2 => 7000");
                conn.commit();
                System.out.println("[A] COMMIT reusit - fara deadlock!");
            } catch (Exception e) {
                System.out.println("[A] Eroare: " + e.getMessage());
            }
        });

        Thread transactionB = new Thread(() -> {
            try {
                Thread.sleep(200);
                try (Connection conn = dao.getNewConnection()) {
                    conn.setAutoCommit(false);
                    System.out.println("[B] BEGIN TRANSACTION (ordonat)");
                    dao.updateSalary(conn, 1, 6500);
                    System.out.println("[B] UPDATE id=1 => 6500 (asteapta lock-ul lui A)");
                    dao.updateSalary(conn, 2, 7500);
                    System.out.println("[B] UPDATE id=2 => 7500");
                    conn.commit();
                    System.out.println("[B] COMMIT reusit - fara deadlock!");
                }
            } catch (Exception e) {
                System.out.println("[B] Eroare: " + e.getMessage());
            }
        });

        transactionA.start();
        transactionB.start();
        transactionA.join();
        transactionB.join();

        System.out.println("\n--- Ambele tranzactii au reusit fara deadlock. ---\n");
    }
}