package com.hotel.client.controllers;

import com.hotel.client.utils.ServiceManager;
import com.hotel.client.utils.SessionManager;
import com.hotel.entities.Facture;
import com.hotel.entities.Reservation;
import com.hotel.enums.*;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.io.IOException;
import java.rmi.RemoteException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EmployeDashboardController {
    private Stage stage;
    private TabPane tabPane;

    // --- DARK LUXURY THEME PALETTE ---
    private final String BG_COLOR = "-fx-background-color: #0f172a;"; // Deep Navy
    private final String CARD_BG = "-fx-background-color: #1e293b; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 0, 4, 0, 0);";
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
        // Inline CSS for TabPane to match dark theme
        tabPane.setStyle("-fx-tab-min-height: 45; -fx-tab-max-height: 45; -fx-background-color: " + BG_COLOR + ";");
        // We inject a stylesheet for deeper customization or use inline logic where possible
        // For this snippet, we assume standard JavaFX CSS behavior but darkened via parent

        tabPane.getTabs().addAll(
                createTab("  Chambres  ", createChambresView()),
                createTab("  Réservations  ", createReservationsView()),
                createTab("  Clients  ", createClientsView()),
                createTab("  Paiements  ", createPaiementsView()),
                createTab("  Rapports  ", createRapportsView())
        );

        // Conditional Admin Tab
        if (SessionManager.estAdmin()) {
            UserManagementController userMgmt = new UserManagementController();
            tabPane.getTabs().add(createTab("  Accès Staff  ", userMgmt.createUserManagementView()));
        }

        // Apply style to tab content area
        root.setCenter(tabPane);

        Scene scene = new Scene(root, 1280, 800);
        // Add minimal CSS for the tab pane headers if possible, otherwise rely on default
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

    // ==========================================
    // 1. VUE CHAMBRES
    // ==========================================
    private VBox createChambresView() {
        VBox view = new VBox(20);
        view.setPadding(new Insets(30));
        view.setStyle(BG_COLOR);

        Label title = new Label("Inventaire des Chambres");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 24px; -fx-font-weight: bold;");

        TableView<Chambre> tableView = createDarkTable();

        // Columns
        addCol(tableView, "ID", "id", 50);
        addCol(tableView, "Numéro", "numero", 100);
        addCol(tableView, "Type", "type", 150);
        addCol(tableView, "Prix (€)", "prix", 100);

        // Custom Status Column
        TableColumn<Chambre, Boolean> dispoCol = new TableColumn<>("État Actuel");
        dispoCol.setCellValueFactory(new PropertyValueFactory<>("disponible"));
        dispoCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null); setGraphic(null);
                } else {
                    Label badge = new Label(item ? " EN SERVICE " : " HORS SERVICE ");
                    badge.setStyle("-fx-background-color: " + (item ? "rgba(34, 197, 94, 0.2)" : "rgba(239, 68, 68, 0.2)") + "; " +
                            "-fx-text-fill: " + (item ? ACCENT_GREEN : ACCENT_RED) + "; " +
                            "-fx-background-radius: 4; -fx-padding: 3 8; -fx-font-weight: bold; -fx-font-size: 10px;");
                    setGraphic(badge);
                    setAlignment(Pos.CENTER_LEFT);
                }
            }
        });
        tableView.getColumns().add(dispoCol);

        // Custom Future Reservations Column
        TableColumn<Chambre, String> nextResCol = new TableColumn<>("Prochaine Réservation");
        nextResCol.setCellValueFactory(data -> {
            try {
                List<Reservation> reservations = ServiceManager.getReservationService().getReservationsParChambre(data.getValue().getId());
                LocalDate now = LocalDate.now();
                Reservation next = reservations.stream()
                        .filter(r -> r.getStatut() != StatutReservation.ANNULEE && r.getDateDepart().isAfter(now))
                        .sorted((r1, r2) -> r1.getDateArrivee().compareTo(r2.getDateArrivee()))
                        .findFirst().orElse(null);
                return new SimpleStringProperty(next != null ? next.getDateArrivee() + " → " + next.getDateDepart() : "Aucune");
            } catch (Exception e) { return new SimpleStringProperty("Erreur"); }
        });
        nextResCol.setPrefWidth(200);
        tableView.getColumns().add(nextResCol);

        // Actions Toolbar
        HBox actions = new HBox(15);
        actions.setAlignment(Pos.CENTER_LEFT);

        Button refreshBtn = createStyledButton("🔄 Actualiser", "#475569");
        refreshBtn.setOnAction(e -> {
            try {
                tableView.setItems(FXCollections.observableArrayList(ServiceManager.getChambreService().getAllChambres()));
            } catch (RemoteException ex) {
                throw new RuntimeException(ex);
            }
        });

        Button addBtn = createStyledButton("＋ Nouvelle Chambre", ACCENT_BLUE);
        addBtn.setOnAction(e -> showAddChambreDialog(tableView));

        Button toggleBtn = createStyledButton("🔧 Changer Statut (Service/HS)", "#eab308"); // Yellow/Amber
        toggleBtn.setOnAction(e -> handleToggleChambreStatus(tableView, refreshBtn));

        actions.getChildren().addAll(refreshBtn, addBtn, toggleBtn);

        view.getChildren().addAll(title, actions, tableView);
        refreshBtn.fire(); // Load data
        return view;
    }

    private void handleToggleChambreStatus(TableView<Chambre> tv, Button refreshBtn) {
        Chambre selected = tv.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert("Sélection requise", "Veuillez sélectionner une chambre."); return; }

        boolean newState = !selected.getDisponible();
        String action = newState ? "Remettre en service" : "Mettre hors service";

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        styleAlert(confirm);
        confirm.setTitle("Maintenance");
        confirm.setHeaderText(action + " : Chambre " + selected.getNumero());
        confirm.setContentText("Confirmez-vous le changement de statut ?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    ServiceManager.getChambreService().mettreAJourDisponibilite(selected.getId(), newState);
                    refreshBtn.fire();
                } catch (Exception ex) { showAlert("Erreur", ex.getMessage()); }
            }
        });
    }

    private void showAddChambreDialog(TableView<Chambre> tableView) {
        Dialog<Chambre> dialog = new Dialog<>();
        dialog.setTitle("Nouvelle Chambre");
        dialog.getDialogPane().setStyle("-fx-background-color: #1e293b; -fx-text-fill: white;");

        ButtonType saveBtn = new ButtonType("Sauvegarder", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(15); grid.setVgap(15); grid.setPadding(new Insets(20));

        TextField numF = createDarkInput("Numéro");
        TextField prixF = createDarkInput("Prix");
        TextField etageF = createDarkInput("Étage");
        TextArea descF = new TextArea(); descF.setPromptText("Description");
        descF.setStyle("-fx-control-inner-background: #334155; -fx-text-fill: white;"); descF.setPrefRowCount(3);

        ComboBox<TypeChambre> typeC = new ComboBox<>(FXCollections.observableArrayList(TypeChambre.values()));
        typeC.setStyle("-fx-background-color: #334155; -fx-mark-color: white;");
        // CSS fix for combo text would be needed here for full dark mode perfection, but functional enough

        grid.add(darkLabel("Numéro:"), 0, 0); grid.add(numF, 1, 0);
        grid.add(darkLabel("Type:"), 0, 1); grid.add(typeC, 1, 1);
        grid.add(darkLabel("Prix:"), 0, 2); grid.add(prixF, 1, 2);
        grid.add(darkLabel("Étage:"), 0, 3); grid.add(etageF, 1, 3);
        grid.add(darkLabel("Description:"), 0, 4); grid.add(descF, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == saveBtn) {
                try {
                    Chambre c = new Chambre();
                    c.setNumero(numF.getText());
                    c.setType(typeC.getValue());
                    c.setPrix(Double.parseDouble(prixF.getText()));
                    c.setEtage(Integer.parseInt(etageF.getText()));
                    c.setDescription(descF.getText());
                    c.setDisponible(true);
                    ServiceManager.getChambreService().ajouterChambre(c);
                    tableView.setItems(FXCollections.observableArrayList(ServiceManager.getChambreService().getAllChambres()));
                    return c;
                } catch (Exception e) { showAlert("Erreur", e.getMessage()); }
            }
            return null;
        });
        dialog.showAndWait();
    }

    // ==========================================
    // 2. VUE RÉSERVATIONS
    // ==========================================
    private VBox createReservationsView() {
        VBox view = new VBox(20); view.setPadding(new Insets(30)); view.setStyle(BG_COLOR);

        Label title = new Label("Gestion des Réservations");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 24px; -fx-font-weight: bold;");

        TableView<Reservation> table = createDarkTable();
        addCol(table, "ID", "id", 50);

        TableColumn<Reservation, String> clientCol = new TableColumn<>("Client");
        clientCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getClient().getNomComplet()));
        table.getColumns().add(clientCol);

        TableColumn<Reservation, String> roomCol = new TableColumn<>("Chambre");
        roomCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getChambre().getNumero()));
        table.getColumns().add(roomCol);

        addCol(table, "Arrivée", "dateArrivee", 100);
        addCol(table, "Départ", "dateDepart", 100);

        TableColumn<Reservation, String> statusCol = new TableColumn<>("Statut");
        statusCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatut().getLibelle()));
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setGraphic(null); }
                else {
                    Label l = new Label(item);
                    String color = "#94a3b8"; // Default gray
                    if(item.contains("Confirm")) color = ACCENT_GREEN;
                    if(item.contains("Attente")) color = "#fbbf24";
                    if(item.contains("Annul")) color = ACCENT_RED;
                    l.setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");
                    setGraphic(l);
                }
            }
        });
        table.getColumns().add(statusCol);

        // Buttons
        HBox actions = new HBox(15);
        Button refresh = createStyledButton("🔄 Actualiser", "#475569");
        refresh.setOnAction(e -> {
            try {
                table.setItems(FXCollections.observableArrayList(ServiceManager.getReservationService().getAllReservations()));
            } catch (RemoteException ex) {
                throw new RuntimeException(ex);
            }
        });

        Button confirm = createStyledButton("✓ Confirmer", ACCENT_GREEN);
        confirm.setOnAction(e -> {
            Reservation r = table.getSelectionModel().getSelectedItem();
            if(r != null) {
                try { ServiceManager.getReservationService().confirmerReservation(r.getId()); refresh.fire(); }
                catch(Exception ex) { showAlert("Erreur", ex.getMessage()); }
            }
        });

        Button cancel = createStyledButton("✕ Annuler", ACCENT_RED);
        cancel.setOnAction(e -> {
            Reservation r = table.getSelectionModel().getSelectedItem();
            if(r != null) {
                try { ServiceManager.getReservationService().annulerReservation(r.getId()); refresh.fire(); }
                catch(Exception ex) { showAlert("Erreur", ex.getMessage()); }
            }
        });

        actions.getChildren().addAll(refresh, confirm, cancel);
        view.getChildren().addAll(title, actions, table);
        refresh.fire();
        return view;
    }

    // ==========================================
    // 3. VUE CLIENTS
    // ==========================================
    private VBox createClientsView() {
        VBox view = new VBox(20); view.setPadding(new Insets(30)); view.setStyle(BG_COLOR);
        Label title = new Label("Base de Données Clients");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 24px; -fx-font-weight: bold;");

        TableView<Client> table = createDarkTable();
        addCol(table, "ID", "id", 50);
        addCol(table, "Nom", "nom", 120);
        addCol(table, "Prénom", "prenom", 120);
        addCol(table, "Email", "email", 200);
        addCol(table, "Téléphone", "telephone", 120);

        Button refresh = createStyledButton("🔄 Actualiser", "#475569");
        refresh.setOnAction(e -> {
            try {
                table.setItems(FXCollections.observableArrayList(ServiceManager.getClientService().getAllClients()));
            } catch (RemoteException ex) {
                throw new RuntimeException(ex);
            }
        });

        view.getChildren().addAll(title, refresh, table);
        refresh.fire();
        return view;
    }

    // ==========================================
    // 4. VUE PAIEMENTS
    // ==========================================
    private VBox createPaiementsView() {
        VBox view = new VBox(20); view.setPadding(new Insets(30)); view.setStyle(BG_COLOR);

        Label title = new Label("Paiements & Facturation");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 24px; -fx-font-weight: bold;");

        // Stats Cards
        HBox statsBox = new HBox(20);
        VBox cardPaid = createStatCard("Total Encaissé", "0.00 €", ACCENT_GREEN);
        VBox cardPending = createStatCard("En Attente", "0.00 €", "#fbbf24");
        statsBox.getChildren().addAll(cardPaid, cardPending);

        // Table
        TableView<Reservation> table = createDarkTable();
        addCol(table, "Réf", "id", 50);
        TableColumn<Reservation, String> clientC = new TableColumn<>("Client");
        clientC.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getClient().getNomComplet()));
        table.getColumns().add(clientC);
        addCol(table, "Montant", "montantTotal", 100);
        TableColumn<Reservation, String> statC = new TableColumn<>("État");
        statC.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatut().getLibelle()));
        table.getColumns().add(statC);

        // Actions
        HBox actions = new HBox(15);
        Button refresh = createStyledButton("🔄 Actualiser", "#475569");

        Button invoice = createStyledButton("📄 Voir Facture", ACCENT_BLUE);
        invoice.setOnAction(e -> {
            Reservation r = table.getSelectionModel().getSelectedItem();
            if(r != null) afficherFacture(r);
            else showAlert("Info", "Sélectionnez une réservation.");
        });

        Button pay = createStyledButton("💳 Marquer comme Payé", ACCENT_GREEN);
        pay.setOnAction(e -> handlePayment(table, refresh));

        // Logic Refresh
        refresh.setOnAction(e -> {
            List<Reservation> all = null;
            try {
                all = ServiceManager.getReservationService().getAllReservations();
            } catch (RemoteException ex) {
                throw new RuntimeException(ex);
            }
            table.setItems(FXCollections.observableArrayList(all));

            double paid = all.stream()
                    .filter(r -> r.getStatut() == StatutReservation.CONFIRMEE || r.getStatut() == StatutReservation.TERMINEE)
                    .mapToDouble(Reservation::getMontantTotal).sum();
            double pending = all.stream()
                    .filter(r -> r.getStatut() == StatutReservation.EN_ATTENTE)
                    .mapToDouble(Reservation::getMontantTotal).sum();

            updateStatCard(cardPaid, String.format("%.2f €", paid));
            updateStatCard(cardPending, String.format("%.2f €", pending));
        });

        actions.getChildren().addAll(refresh, invoice, pay);
        view.getChildren().addAll(title, statsBox, new Separator(), new Label("Transactions récentes"), actions, table);
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
            root.setStyle("-fx-background-color: white;"); // White paper look

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

    // ==========================================
    // 5. VUE RAPPORTS
    // ==========================================
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

    // ==========================================
    // HELPERS & STYLES
    // ==========================================

    private TableView createDarkTable() {
        TableView tv = new TableView();
        tv.setStyle(TABLE_STYLE);
        tv.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        return tv;
    }

    private void addCol(TableView tv, String title, String prop, int width) {
        TableColumn col = new TableColumn(title);
        col.setCellValueFactory(new PropertyValueFactory<>(prop));
        tv.getColumns().add(col);
    }

    private Button createStyledButton(String text, String colorHex) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color: " + colorHex + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-cursor: hand; -fx-padding: 8 15;");
        return b;
    }

    private TextField createDarkInput(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setStyle("-fx-background-color: #334155; -fx-text-fill: white; -fx-prompt-text-fill: #94a3b8;");
        return tf;
    }

    private Label darkLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill: #cbd5e1;");
        return l;
    }

    private VBox createStatCard(String title, String value, String accentColor) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(20));
        card.setPrefWidth(200);
        card.setStyle("-fx-background-color: #1e293b; -fx-background-radius: 8; -fx-border-color: " + accentColor + "; -fx-border-width: 0 0 0 4;");

        Label t = new Label(title);
        t.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px;");

        Label v = new Label(value);
        v.setStyle("-fx-text-fill: white; -fx-font-size: 24px; -fx-font-weight: bold;");

        card.getChildren().addAll(t, v);
        return card;
    }

    private void updateStatCard(VBox card, String newValue) {
        // Safe cast assuming structure
        if(card.getChildren().size() > 1 && card.getChildren().get(1) instanceof Label) {
            ((Label) card.getChildren().get(1)).setText(newValue);
        }
    }

    private void styleDatePicker(DatePicker dp) {
        dp.setStyle("-fx-background-color: #334155; -fx-control-inner-background: #334155;");
        dp.getEditor().setStyle("-fx-background-color: #334155; -fx-text-fill: white;");
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

    // Inner class for stats
    public static class ClientStat {
        private String nomComplet;
        private Integer nombreReservations;
        private Double montantTotal;

        public ClientStat(String n, Integer c, Double t) {
            this.nomComplet = n; this.nombreReservations = c; this.montantTotal = t;
        }
        public String getNomComplet() { return nomComplet; }
        public Integer getNombreReservations() { return nombreReservations; }
        public Double getMontantTotal() { return montantTotal; }
    }
}