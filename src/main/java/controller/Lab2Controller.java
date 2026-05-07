package controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.Label;
import repository.TransactionDAO;
import service.BatchPerformanceService;
import service.ConcurrencyDemoService;
import service.DeadlockDemoService;

import java.io.PrintStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class Lab2Controller {

    @FXML private TextArea outputArea;
    @FXML private Label statusLabel;

    private ConcurrencyDemoService concurrencyService;
    private DeadlockDemoService deadlockService;
    private BatchPerformanceService batchService;

    @FXML
    public void initialize() {
        TransactionDAO dao = new TransactionDAO();
        concurrencyService = new ConcurrencyDemoService(dao);
        deadlockService = new DeadlockDemoService(dao);
        batchService = new BatchPerformanceService(dao);
        redirectSystemOut();
    }

    private void redirectSystemOut() {
        OutputStream out = new OutputStream() {
            private final StringBuilder buffer = new StringBuilder();

            @Override
            public void write(int b) {
                char c = (char) b;
                buffer.append(c);
                if (c == '\n') {
                    String line = buffer.toString();
                    buffer.setLength(0);
                    Platform.runLater(() -> {
                        outputArea.appendText(line);
                        outputArea.setScrollTop(Double.MAX_VALUE);
                    });
                }
            }
        };
        System.setOut(new PrintStream(out, true, StandardCharsets.UTF_8));
    }

    @FXML
    private void runDirtyRead() {
        runInBackground("Dirty Read", () -> concurrencyService.demonstrateDirtyRead());
    }

    @FXML
    private void runNonRepeatableRead() {
        runInBackground("Non-Repeatable Read", () -> concurrencyService.demonstrateNonRepeatableRead());
    }

    @FXML
    private void runPhantomRead() {
        runInBackground("Phantom Read", () -> concurrencyService.demonstratePhantomRead());
    }

    @FXML
    private void runLostUpdate() {
        runInBackground("Lost Update", () -> concurrencyService.demonstrateLostUpdate());
    }

    @FXML
    private void runDeadlock() {
        runInBackground("Deadlock", () -> deadlockService.demonstrateDeadlock());
    }

    @FXML
    private void runBatch() {
        runInBackground("Comparatie Batch", () -> batchService.runComparison());
    }

    @FXML
    private void runAll() {
        runInBackground("Toate demonstratiile", () -> {
            concurrencyService.demonstrateDirtyRead();
            concurrencyService.demonstrateNonRepeatableRead();
            concurrencyService.demonstratePhantomRead();
            concurrencyService.demonstrateLostUpdate();
            deadlockService.demonstrateDeadlock();
            batchService.runComparison();
        });
    }

    @FXML
    private void clearOutput() {
        outputArea.clear();
        statusLabel.setText("");
    }

    private void runInBackground(String name, DemoTask task) {
        Platform.runLater(() -> statusLabel.setText("Se ruleaza: " + name + "..."));
        Thread thread = new Thread(() -> {
            try {
                task.run();
                Platform.runLater(() -> statusLabel.setText(name + " - finalizat."));
            } catch (Exception e) {
                Platform.runLater(() -> {
                    outputArea.appendText("\n[EROARE] " + name + ": " + e.getMessage() + "\n");
                    statusLabel.setText(name + " - eroare!");
                });
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    @FunctionalInterface
    private interface DemoTask {
        void run() throws Exception;
    }
}