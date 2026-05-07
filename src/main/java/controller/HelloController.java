package controller;

import domain.Cofetar;
import domain.Tort;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import repository.DataAccessObject;
import service.CofetarieService;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class HelloController {

    @FXML private TableView<Cofetar> tabelParinte;
    @FXML private TableView<Tort> tabelCopil;
    @FXML private TextField txtCautare;
    @FXML private TextField txtDenumire;
    @FXML private TextField txtPret;
    @FXML private TextField txtClientId;

    private CofetarieService service;
    private List<Tort> torturiCurente;

    @FXML
    public void initialize() {
        service = new CofetarieService(new DataAccessObject());
        initColumnsCofetar();
        initColumnsTort();
        refreshDate();
        tabelParinte.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> { if (newVal != null) loadTorturi(newVal.getId()); }
        );
    }

    private void initColumnsCofetar() {
        TableColumn<Cofetar, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getId()).asObject());
        TableColumn<Cofetar, String> colNume = new TableColumn<>("Nume");
        colNume.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNume()));
        TableColumn<Cofetar, String> colSpec = new TableColumn<>("Specializare");
        colSpec.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getSpecializare()));
        tabelParinte.getColumns().setAll(colId, colNume, colSpec);
    }

    private void initColumnsTort() {
        TableColumn<Tort, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getId()).asObject());
        TableColumn<Tort, String> colDen = new TableColumn<>("Denumire");
        colDen.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDenumire()));
        TableColumn<Tort, Double> colPret = new TableColumn<>("Pret");
        colPret.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getPret()).asObject());
        TableColumn<Tort, Integer> colClient = new TableColumn<>("Client ID");
        colClient.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getClientId()).asObject());
        tabelCopil.getColumns().setAll(colId, colDen, colPret, colClient);
    }

    @FXML
    public void refreshDate() {
        try {
            List<Cofetar> cofetari = service.getAllCofetari();
            tabelParinte.setItems(FXCollections.observableArrayList(cofetari));
        } catch (SQLException e) {
            showAlert("Eroare la incarcare cofetari: " + e.getMessage());
        }
    }

    private void loadTorturi(int cofetarId) {
        try {
            torturiCurente = service.getTorturiByCofetar(cofetarId);
            tabelCopil.setItems(FXCollections.observableArrayList(torturiCurente));
        } catch (SQLException e) {
            showAlert("Eroare la incarcare torturi: " + e.getMessage());
        }
    }

    @FXML
    public void filtreazaTorturi() {
        if (torturiCurente == null) return;
        String filtru = txtCautare.getText().toLowerCase().trim();
        List<Tort> filtrate = torturiCurente.stream()
                .filter(t -> t.getDenumire().toLowerCase().contains(filtru))
                .collect(Collectors.toList());
        tabelCopil.setItems(FXCollections.observableArrayList(filtrate));
    }

    @FXML
    public void adaugaTort() {
        Cofetar selected = tabelParinte.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert("Selecteaza un cofetar."); return; }
        try {
            String denumire = txtDenumire.getText();
            double pret = Double.parseDouble(txtPret.getText());
            int clientId = Integer.parseInt(txtClientId.getText());
            service.adaugaTort(denumire, pret, selected.getId(), clientId);
            loadTorturi(selected.getId());
            clearFields();
        } catch (Exception e) {
            showAlert("Eroare la adaugare: " + e.getMessage());
        }
    }

    @FXML
    public void editeazaTort() {
        Tort selected = tabelCopil.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert("Selecteaza un tort."); return; }
        try {
            String denumire = txtDenumire.getText();
            double pret = Double.parseDouble(txtPret.getText());
            service.editeazaTort(selected.getId(), denumire, pret);
            Cofetar cofetar = tabelParinte.getSelectionModel().getSelectedItem();
            if (cofetar != null) loadTorturi(cofetar.getId());
            clearFields();
        } catch (Exception e) {
            showAlert("Eroare la editare: " + e.getMessage());
        }
    }

    @FXML
    public void stergeTort() {
        Tort selected = tabelCopil.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert("Selecteaza un tort."); return; }
        try {
            service.stergeTort(selected.getId());
            Cofetar cofetar = tabelParinte.getSelectionModel().getSelectedItem();
            if (cofetar != null) loadTorturi(cofetar.getId());
        } catch (Exception e) {
            showAlert("Eroare la stergere: " + e.getMessage());
        }
    }

    @FXML
    public void openLab2() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/lab2-view.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = new Stage();
            stage.setTitle("Lab 2 - Tranzactii si Niveluri de Izolare");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            showAlert("Nu s-a putut deschide fereastra Lab 2: " + e.getMessage());
        }
    }

    private void clearFields() {
        txtDenumire.clear();
        txtPret.clear();
        txtClientId.clear();
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        alert.showAndWait();
    }
}