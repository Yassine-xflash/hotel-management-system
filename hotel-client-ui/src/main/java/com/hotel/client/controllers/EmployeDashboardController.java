package com.hotel.client.controllers;

import com.hotel.client.utils.ServiceManager;
import com.hotel.client.utils.SessionManager;
import com.hotel.entities.Facture;
import com.hotel.entities.Paiement;
import com.hotel.entities.Reservation;
import com.hotel.enums.StatutReservation;
import com.hotel.enums.TypePaiement;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.io.IOException;
import java.rmi.RemoteException;
import java.time.LocalDate;
import java.util.List;

public class EmployeDashboardController {
    private Stage stage;
    private TabPane tabPane;

    // --- DARK LUXURY THEME PALETTE ---
    private final String BG_COLOR = "-fx-background-color: #0f172a;"; // Deep Navy
    private final String ACCENT_GOLD = "#fbbf24";
    private final String ACCENT_BLUE = "#3b82f6";
    private final String ACCENT_RED = "#ef4444";
    private final String ACCENT_GREEN = "#22c55e";
    private final String TEXT_WHITE = "-fx-text-fill: #f8fafc;";
    private final String TEXT_MUTED = "-fx-text-fill: #94a3b8;";

    // CSS for TableView to remove grid lines and add dark style
    private final String TABLE_STYLE =
            "-fx-background-color: transparent; " +
                    "-fx-base: #1e293b; " +
                    "-fx-control-inner-background: #1e293b; " +
                    "-fx-table-cell-border-color: transparent; " +
                    "-fx-table-header-border-color: #334155; " +
                    "-fx-padding: 5;";

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void show() {
        BorderPane root = new BorderPane();
        root.setStyle(BG_COLOR);

        // --- HEADER ---
        HBox header = new HBox(20);
        header.setPadding(new Insets(15, 30, 15, 30));
        header.setStyle("-fx-background-color: #1e293b; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 0, 5, 0, 0);");
        header.setAlignment(Pos.CENTER_LEFT);

        Label brand = new Label("ROYAL SUITE | ADMINISTRATION");
        brand.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        brand.setStyle("-fx-text-fill: " + ACCENT_GOLD + ";");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        VBox userInfo = new VBox(2);
        userInfo.setAlignment(Pos.CENTER_RIGHT);
        Label userName = new Label(SessionManager.getUtilisateurConnecte().getUsername().toUpperCase());
        userName.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        Label userRole = new Label(SessionManager.getUtilisateurConnecte().getRole().getLibelle());
        userRole.setStyle(TEXT_MUTED + "-fx-font-size: 11px;");
        userInfo.getChildren().addAll(userName, userRole);

        Button logoutButton = new Button("Déconnexion");
        logoutButton.setStyle("-fx-background-color: transparent; -fx-text-fill: " + ACCENT_RED + "; -fx-border-color: " + ACCENT_RED + "; -fx-border-radius: 5; -fx-cursor: hand;");
        logoutButton.setOnAction(e -> {
            SessionManager.deconnecter();
            stage.close();
            LoginController controller = new LoginController();
            Stage loginStage = new Stage();
            controller.setStage(loginStage);
            controller.show();
        });

        header.getChildren().addAll(brand, spacer, userInfo, logoutButton);
        root.setTop(header);

        // --- TABS ---
        tabPane = new TabPane();
        tabPane.setStyle("-fx-tab-min-height: 45; -fx-tab-max-height: 45; -fx-background-color: " + BG_COLOR + ";");

        tabPane.getTabs().addAll(
                createTab("  Réservations  ", createReservationsView()),
                createTab("  Paiements & Facturation  ", createPaiementsView()),
                createTab("  Rapports  ", createRapportsView())
        );

        // Conditional Admin Tab
        if (SessionManager.estAdmin()) {
            UserManagementController userMgmt = new UserManagementController();
            tabPane.getTabs().add(createTab("  Accès Staff  ", userMgmt.createUserManagementView()));
        }

        root.setCenter(tabPane);

        Scene scene = new Scene(root, 1280, 800);
        stage.setTitle("Tableau de bord - " + SessionManager.getUtilisateurConnecte().getUsername());
        stage.setScene(scene);
        stage.show();

        stage.setOnCloseRequest(e -> SessionManager.deconnecter());
    }

    private Tab createTab(String title, VBox content) {
        Tab tab = new Tab(title);
        tab.setContent(content);
        tab.setClosable(false);
        return tab;
    }

    private VBox createReservationsView() {
        VBox view = new VBox(20);
        view.setPadding(new Insets(30));
        view.setStyle(BG_COLOR);
        Label title = new Label("Gestion des Réservations");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 24px; -fx-font-weight: bold;");
        view.getChildren().add(title);
        return view;
    }

    private VBox createPaiementsView() {
        VBox view = new VBox(20);
        view.setPadding(new Insets(30));
        view.setStyle(BG_COLOR);

        Label title = new Label("Paiements & Facturation");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 24px; -fx-font-weight: bold;");

        TableView<Reservation> table = new TableView<>();
        table.setStyle(TABLE_STYLE);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Reservation, Integer> idCol = new TableColumn<>("Réf");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        table.getColumns().add(idCol);

        TableColumn<Reservation, String> clientC = new TableColumn<>("Client");
        clientC.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getClient().getNomComplet()));
        table.getColumns().add(clientC);

        TableColumn<Reservation, Double> montantCol = new TableColumn<>("Montant");
        montantCol.setCellValueFactory(new PropertyValueFactory<>("montantTotal"));
        table.getColumns().add(montantCol);

        TableColumn<Reservation, String> statC = new TableColumn<>("État");
        statC.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatut().getLibelle()));
        table.getColumns().add(statC);

        HBox actions = new HBox(15);
        Button refresh = new Button("🔄 Actualiser");
        refresh.setStyle("-fx-background-color: #475569; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-cursor: hand; -fx-padding: 8 15;");

        Button invoice = new Button("📄 Voir Facture");
        invoice.setStyle("-fx-background-color: " + ACCENT_BLUE + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-cursor: hand; -fx-padding: 8 15;");
        invoice.setOnAction(e -> {
            Reservation r = table.getSelectionModel().getSelectedItem();
            if (r != null) afficherFacture(r);
            else showAlert("Info", "Sélectionnez une réservation.");
        });

        Button pay = new Button("💳 Marquer comme Payé");
        pay.setStyle("-fx-background-color: " + ACCENT_GREEN + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-cursor: hand; -fx-padding: 8 15;");
        pay.setOnAction(e -> handlePayment(table, refresh));

        refresh.setOnAction(e -> {
            try {
                List<Reservation> all = ServiceManager.getReservationService().getAllReservations();
                table.setItems(FXCollections.observableArrayList(all));
            } catch (RemoteException ex) {
                throw new RuntimeException(ex);
            }
        });

        actions.getChildren().addAll(refresh, invoice, pay);
        view.getChildren().addAll(title, new Separator(), new Label("Transactions récentes"), actions, table);
        refresh.fire();
        return view;
    }

    private void handlePayment(TableView<Reservation> table, Button refresh) {
        Reservation r = table.getSelectionModel().getSelectedItem();
        if (r == null) {
            showAlert("Info", "Sélectionnez une réservation.");
            return;
        }

        Dialog<TypePaiement> dialog = new Dialog<>();
        dialog.setTitle("Encaissement");
        dialog.setHeaderText("Montant à encaisser : " + r.getMontantTotal() + " €");
        dialog.getDialogPane().setStyle("-fx-background-color: #1e293b; -fx-text-fill: white;");

        ButtonType saveBtn = new ButtonType("Confirmer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        ComboBox<TypePaiement> typeC = new ComboBox<>(FXCollections.observableArrayList(TypePaiement.values()));
        typeC.setStyle("-fx-background-color: #334155; -fx-mark-color: white;");
        dialog.getDialogPane().setContent(typeC);

        dialog.setResultConverter(btn -> {
            if (btn == saveBtn) {
                return typeC.getValue();
            }
            return null;
        });

        dialog.showAndWait().ifPresent(typePaiement -> {
            if (typePaiement != null) {
                try {
                    Paiement p = new Paiement(r, r.getMontantTotal(), typePaiement);
                    p = ServiceManager.getPaiementService().creerPaiement(p);
                    ServiceManager.getPaiementService().traiterPaiement(p.getId(), typePaiement);
                    ServiceManager.getReservationService().confirmerReservation(r.getId());
                    refresh.fire();
                } catch (Exception ex) {
                    showAlert("Erreur", ex.getMessage());
                }
            }
        });
    }

    private void afficherFacture(Reservation r) {
        try {
            Facture facture = ServiceManager.getFactureService().findByReservation(r);
            if (facture == null) {
                facture = ServiceManager.getFactureService().genererFacture(r);
            }

            Stage fStage = new Stage();
            VBox root = new VBox(20);
            root.setPadding(new Insets(40));
            root.setStyle("-fx-background-color: white;");

            Label header = new Label("HÔTEL ROYAL SUITE");
            header.setFont(Font.font("Times New Roman", FontWeight.BOLD, 24));

            Label sub = new Label("FACTURE N° " + facture.getNumeroFacture() + "\nDate: " + facture.getDateFacture());

            GridPane details = new GridPane();
            details.setHgap(20);
            details.setVgap(10);
            details.add(new Label("Client:"), 0, 0);
            details.add(new Label(r.getClient().getNomComplet()), 1, 0);
            details.add(new Label("Chambre:"), 0, 1);
            details.add(new Label(r.getChambre().getNumero() + " (" + r.getChambre().getType() + ")"), 1, 1);
            details.add(new Label("Séjour:"), 0, 2);
            details.add(new Label(r.getDateArrivee() + " au " + r.getDateDepart()), 1, 2);

            Label total = new Label("TOTAL TTC: " + String.format("%.2f €", facture.getMontant()));
            total.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #0f172a; -fx-border-color: black; -fx-border-width: 2 0 0 0; -fx-padding: 10 0 0 0;");

            Button print = new Button("Fermer");
            print.setOnAction(e -> fStage.close());

            root.getChildren().addAll(header, new Separator(), sub, details, new Separator(), total, print);
            fStage.setScene(new Scene(root, 400, 600));
            fStage.setTitle("Facture");
            fStage.show();
        } catch (Exception e) {
            showAlert("Erreur", "Impossible de générer ou d'afficher la facture.\n" + e.getMessage());
        }
    }

    private VBox createRapportsView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hotel/client/reporting-view.fxml"));
            VBox view = loader.load();
            view.setPadding(new Insets(30));
            view.setStyle(BG_COLOR);
            return view;
        } catch (IOException e) {
            e.printStackTrace();
            return new VBox();
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        styleAlert(alert);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void styleAlert(Alert alert) {
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #1e293b; -fx-text-fill: white;");
        dialogPane.lookup(".content.label").setStyle("-fx-text-fill: white;");
        dialogPane.lookup(".header-panel").setStyle("-fx-background-color: #0f172a;");
    }
}
