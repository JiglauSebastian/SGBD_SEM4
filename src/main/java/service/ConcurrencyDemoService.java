package service;

import repository.TransactionDAO;

import java.sql.*;

public class ConcurrencyDemoService {

    private final TransactionDAO dao;

    public ConcurrencyDemoService(TransactionDAO dao) {
        this.dao = dao;
    }

    public void demonstrateDirtyRead() throws InterruptedException {
        System.out.println("\n========== DIRTY READ DEMO ==========");
        System.out.println("Scenariu: Tranzactia A updateaza dar nu face commit.");
        System.out.println("         Tranzactia B citeste datele necomise.\n");

        Object syncA = new Object();
        Object syncB = new Object();

        Thread transactionA = new Thread(() -> {
            try (Connection conn = dao.getNewConnection()) {
                conn.setAutoCommit(false);
                System.out.println("[A] BEGIN TRANSACTION");
                dao.updateSalary(conn, 1, 10000);
                System.out.println("[A] UPDATE salary = 10000 WHERE id = 1 (ne-comis)");

                synchronized (syncA) { syncA.notifyAll(); }
                synchronized (syncB) { syncB.wait(3000); }

                conn.rollback();
                System.out.println("[A] ROLLBACK efectuat! Valoarea revine la original.");
            } catch (Exception e) {
                System.out.println("[A] Eroare: " + e.getMessage());
            }
        });

        Thread transactionB = new Thread(() -> {
            synchronized (syncA) {
                try { syncA.wait(3000); } catch (InterruptedException ignored) {}
            }
            System.out.println("\n[B] Tranzactia B incepe sa citeasca...");
            try (Connection conn = dao.getNewConnection()) {
                conn.setAutoCommit(false);
                conn.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
                System.out.println("[B] BEGIN TRANSACTION (READ UNCOMMITTED)");
                double salary = dao.getSalary(conn, 1);
                System.out.println("[B] SELECT salary WHERE id = 1 => " + salary);
                System.out.println("[B] *** Dirty Read: a citit valoarea necomisa 10000! ***");
                conn.commit();
                System.out.println("[B] COMMIT");
            } catch (Exception e) {
                System.out.println("[B] Eroare: " + e.getMessage());
            }
            synchronized (syncB) { syncB.notifyAll(); }
        });

        transactionA.start();
        Thread.sleep(200);
        transactionB.start();
        transactionA.join();
        transactionB.join();

    }

    public void demonstrateNonRepeatableRead() throws InterruptedException {
        System.out.println("\n========== NON-REPEATABLE READ DEMO ==========");
        System.out.println("Scenariu: Tranzactia A citeste de doua ori acelasi rand.");
        System.out.println("         Tranzactia B modifica randul intre cele doua citiri.\n");

        try { dao.resetSalary(1, 5000); } catch (Exception ignored) {}

        Object afterFirstRead = new Object();
        Object afterBCommit = new Object();

        Thread transactionA = new Thread(() -> {
            try (Connection conn = dao.getNewConnection()) {
                conn.setAutoCommit(false);
                conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
                System.out.println("[A] BEGIN TRANSACTION (READ COMMITTED)");

                double first = dao.getSalary(conn, 1);
                System.out.println("[A] Prima citire: salary = " + first);

                synchronized (afterFirstRead) { afterFirstRead.notifyAll(); }
                synchronized (afterBCommit) {
                    try { afterBCommit.wait(5000); } catch (InterruptedException ignored) {}
                }

                double second = dao.getSalary(conn, 1);
                System.out.println("[A] A doua citire: salary = " + second);
                if (first != second) {
                    System.out.println("[A] *** Non-Repeatable Read: valorile difera! " + first + " vs " + second + " ***");
                } else {
                    System.out.println("[A] Valorile sunt identice.");
                }
                conn.commit();
                System.out.println("[A] COMMIT");
            } catch (Exception e) {
                System.out.println("[A] Eroare: " + e.getMessage());
            }
        });

        Thread transactionB = new Thread(() -> {
            synchronized (afterFirstRead) {
                try { afterFirstRead.wait(5000); } catch (InterruptedException ignored) {}
            }
            try (Connection conn = dao.getNewConnection()) {
                conn.setAutoCommit(false);
                System.out.println("\n[B] BEGIN TRANSACTION");
                dao.updateSalary(conn, 1, 12000);
                conn.commit();
                System.out.println("[B] UPDATE salary = 12000, COMMIT efectuat");
            } catch (Exception e) {
                System.out.println("[B] Eroare: " + e.getMessage());
            }
            synchronized (afterBCommit) { afterBCommit.notifyAll(); }
        });

        transactionA.start();
        Thread.sleep(300);
        transactionB.start();
        transactionA.join();
        transactionB.join();

        try { dao.resetSalary(1, 5000); } catch (Exception ignored) {}

    }

    public void demonstratePhantomRead() throws InterruptedException {
        System.out.println("\n========== PHANTOM READ DEMO ==========");
        System.out.println("Scenariu: Tranzactia A numara randuri de doua ori.");
        System.out.println("         Tranzactia B insereaza un rand nou intre numaratori.\n");

        Object afterFirstCount = new Object();
        Object afterBCommit = new Object();

        Thread transactionA = new Thread(() -> {
            try (Connection conn = dao.getNewConnection()) {
                conn.setAutoCommit(false);
                conn.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
                System.out.println("[A] BEGIN TRANSACTION (REPEATABLE READ)");

                int first = dao.countByDepartment(conn, 5);
                System.out.println("[A] Prima numaratoare dept_id=5: " + first);

                synchronized (afterFirstCount) { afterFirstCount.notifyAll(); }
                synchronized (afterBCommit) {
                    try { afterBCommit.wait(5000); } catch (InterruptedException ignored) {}
                }

                int second = dao.countByDepartment(conn, 5);
                System.out.println("[A] A doua numaratoare dept_id=5: " + second);
                if (second != first) {
                    System.out.println("[A] *** Phantom Read: numarul de randuri s-a schimbat! " + first + " -> " + second + " ***");
                } else {
                    System.out.println("[A] Numarul de randuri este identic (izolare buna).");
                }
                conn.commit();
                System.out.println("[A] COMMIT");
            } catch (Exception e) {
                System.out.println("[A] Eroare: " + e.getMessage());
            }
        });

        Thread transactionB = new Thread(() -> {
            synchronized (afterFirstCount) {
                try { afterFirstCount.wait(5000); } catch (InterruptedException ignored) {}
            }
            try (Connection conn = dao.getNewConnection()) {
                conn.setAutoCommit(false);
                System.out.println("\n[B] BEGIN TRANSACTION");
                dao.insertEmployee(conn, "Angajat Nou", 5);
                conn.commit();
                System.out.println("[B] INSERT angajat nou in dept 5, COMMIT efectuat");
            } catch (Exception e) {
                System.out.println("[B] Eroare: " + e.getMessage());
            }
            synchronized (afterBCommit) { afterBCommit.notifyAll(); }
        });

        transactionA.start();
        Thread.sleep(200);
        transactionB.start();
        transactionA.join();
        transactionB.join();

    }

    public void demonstrateLostUpdate() throws InterruptedException {
        System.out.println("\n========== LOST UPDATE DEMO ==========");
        System.out.println("Scenariu: Doua tranzactii citesc aceeasi valoare,");
        System.out.println("         calculeaza un nou salariu si scriu inapoi.");
        System.out.println("         Una dintre actualizari se pierde.\n");

        try { dao.resetSalary(1, 5000); } catch (Exception ignored) {}

        Object afterARead = new Object();
        Object afterBCommit = new Object();

        Thread transactionA = new Thread(() -> {
            try (Connection conn = dao.getNewConnection()) {
                conn.setAutoCommit(false);
                System.out.println("[A] BEGIN TRANSACTION");
                double salary = dao.getSalary(conn, 1);
                System.out.println("[A] SELECT salary = " + salary);
                double newSalary = salary + 1000;
                System.out.println("[A] Calculeaza newSalary = " + salary + " + 1000 = " + newSalary);

                synchronized (afterARead) { afterARead.notifyAll(); }
                synchronized (afterBCommit) {
                    try { afterBCommit.wait(5000); } catch (InterruptedException ignored) {}
                }

                dao.updateSalary(conn, 1, newSalary);
                conn.commit();
                System.out.println("[A] UPDATE salary = " + newSalary + ", COMMIT");
            } catch (Exception e) {
                System.out.println("[A] Eroare: " + e.getMessage());
            }
        });

        Thread transactionB = new Thread(() -> {
            synchronized (afterARead) {
                try { afterARead.wait(3000); } catch (InterruptedException ignored) {}
            }
            try (Connection conn = dao.getNewConnection()) {
                conn.setAutoCommit(false);
                System.out.println("\n[B] BEGIN TRANSACTION");
                double salary = dao.getSalary(conn, 1);
                System.out.println("[B] SELECT salary = " + salary);
                double newSalary = salary + 500;
                System.out.println("[B] Calculeaza newSalary = " + salary + " + 500 = " + newSalary);
                dao.updateSalary(conn, 1, newSalary);
                conn.commit();
                System.out.println("[B] UPDATE salary = " + newSalary + ", COMMIT");
            } catch (Exception e) {
                System.out.println("[B] Eroare: " + e.getMessage());
            }
            synchronized (afterBCommit) { afterBCommit.notifyAll(); }
        });

        transactionA.start();
        Thread.sleep(200);
        transactionB.start();
        transactionA.join();
        transactionB.join();

        try (Connection conn = dao.getNewConnection()) {
            conn.setAutoCommit(true);
            double finalSalary = dao.getSalary(conn, 1);
            System.out.println("\n[Stare finala] salary id=1 = " + finalSalary);
            System.out.println("*** Lost Update: Expected 6500 (5000+1000+500), Got " + finalSalary + " ***");
        } catch (Exception e) {
            System.out.println("Eroare citire stare finala: " + e.getMessage());
        }

        try { dao.resetSalary(1, 5000); } catch (Exception ignored) {}

    }
}