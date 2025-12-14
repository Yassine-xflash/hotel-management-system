package com.hotel.client.controllers;

import com.hotel.client.utils.ServiceManager;
import com.hotel.client.utils.SessionManager;
import com.hotel.entities.Chambre;
import com.hotel.entities.Client;
import com.hotel.entities.Facture;
import com.hotel.entities.Reservation;
import com.hotel.enums.TypeChambre;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.util.Callback;

import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class ClientDashboardController {
    private Stage stage;
    private DatePicker dateArriveePicker;
    private DatePicker dateDepartPicker;
    private ListView<ChambreItem> resultListView;

    // Theme Constants
    private final String SIDEBAR_BG = "-fx-background-color: #0f172a;";
    private final String CONTENT_BG = "-fx-background-color: #1e293b;";
    private final String ACCENT_COLOR = "#fbbf24";
    private final String BTN_STYLE = "-fx-background-color: transparent; -fx-text-fill: #94a3b8; -fx-alignment: CENTER_LEFT; -fx-font-size: 14px; -fx-padding: 15;";
    private final String BTN_HOVER = "-fx-background-color: #334155; -fx-text-fill: white; -fx-alignment: CENTER_LEFT; -fx-font-size: 14px; -fx-padding: 15;";

    private static class ChambreItem {
        Chambre chambre;
        ChambreItem(Chambre chambre) { this.chambre = chambre; }
        @Override public String toString() { return "Suite " + chambre.getNumero(); } // Used for internal reference
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void show() {
        BorderPane root = new BorderPane();

        // --- SIDEBAR (LEFT) ---
        VBox sidebar = new VBox(10);
        sidebar.setPrefWidth(250);
        sidebar.setStyle(SIDEBAR_BG);
        sidebar.setPadding(new Insets(20, 0, 0, 0));

        Label brand = new Label("ROYAL SUITE");
        brand.setStyle("-fx-text-fill: " + ACCENT_COLOR + "; -fx-font-weight: bold; -fx-font-size: 20px; -fx-padding: 0 0 20 20;");

        Label userMsg = new Label("Bonjour, " + SessionManager.getUtilisateurConnecte().getUsername());
        userMsg.setStyle("-fx-text-fill: #64748b; -fx-font-size: 12px; -fx-padding: 0 0 30 20;");

        Button navBook = createNavButton("🔍  Nouvelle Réservation");
        navBook.setStyle("-fx-background-color: #334155; -fx-text-fill: " + ACCENT_COLOR + "; -fx-border-color: " + ACCENT_COLOR + "; -fx-border-width: 0 0 0 4; -fx-alignment: CENTER_LEFT; -fx-padding: 15;");

        Button navMyRes = createNavButton("🎫  Mes Réservations");
        navMyRes.setOnAction(e -> afficherMesReservations());

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button btnLogout = createNavButton("🚪  Déconnexion");
        btnLogout.setStyle("-fx-text-fill: #ef4444; -fx-background-color: transparent; -fx-alignment: CENTER_LEFT; -fx-padding: 15;");
        btnLogout.setOnAction(e -> {
            SessionManager.deconnecter();
            stage.close(); // Close the current dashboard stage
            LoginController loginController = new LoginController();
            Stage loginStage = new Stage();
            loginStage.setMaximized(true); // Add this line
            loginController.setStage(loginStage);
            loginController.show(); // Show a single new login stage
        });

        sidebar.getChildren().addAll(brand, userMsg, navBook, navMyRes, spacer, btnLogout);
        root.setLeft(sidebar);

        // --- MAIN CONTENT (CENTER) ---
        VBox content = new VBox(25);
        content.setStyle(CONTENT_BG);
        content.setPadding(new Insets(40));

        Label pageTitle = new Label("Trouvez votre séjour idéal");
        pageTitle.setStyle("-fx-text-fill: white; -fx-font-size: 28px; -fx-font-weight: bold;");

        // Search Bar styled as a horizontal strip
        HBox searchBar = new HBox(15);
        searchBar.setAlignment(Pos.CENTER_LEFT);
        searchBar.setStyle("-fx-background-color: #0f172a; -fx-padding: 20; -fx-background-radius: 10;");

        dateArriveePicker = new DatePicker(LocalDate.now().plusDays(1));
        dateDepartPicker = new DatePicker(LocalDate.now().plusDays(2));
        styleDatePicker(dateArriveePicker);
        styleDatePicker(dateDepartPicker);

        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().add("Toutes");
        for (TypeChambre type : TypeChambre.values()) typeCombo.getItems().add(type.getLibelle());
        typeCombo.setValue("Toutes");
        typeCombo.setStyle("-fx-background-color: #334155; -fx-text-fill: white;");

        Spinner<Integer> personnesSpinner = new Spinner<>(1, 10, 1);
        personnesSpinner.setStyle("-fx-background-color: #334155;");

        Button btnSearch = new Button("Rechercher");
        btnSearch.setStyle("-fx-background-color: " + ACCENT_COLOR + "; -fx-text-fill: #0f172a; -fx-font-weight: bold; -fx-padding: 8 20;");

        searchBar.getChildren().addAll(
                createLabel("Du"), dateArriveePicker,
                createLabel("Au"), dateDepartPicker,
                typeCombo, personnesSpinner, btnSearch
        );

        // Result List with Custom Cell Factory for "Card" look
        resultListView = new ListView<>();
        resultListView.setStyle("-fx-background-color: transparent; -fx-control-inner-background: #1e293b;");
        resultListView.setCellFactory(param -> new ListCell<ChambreItem>() {
            @Override
            protected void updateItem(ChambreItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    HBox card = new HBox(20);
                    card.setAlignment(Pos.CENTER_LEFT);
                    card.setPadding(new Insets(15));
                    card.setStyle("-fx-background-color: #334155; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 5, 0, 0, 2);");

                    VBox details = new VBox(5);
                    Label title = new Label("Suite " + item.chambre.getNumero());
                    title.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");
                    Label type = new Label(item.chambre.getType().getLibelle() + " • Étage " + item.chambre.getEtage());
                    type.setStyle("-fx-text-fill: #94a3b8;");
                    details.getChildren().addAll(title, type);

                    Region r = new Region();
                    HBox.setHgrow(r, Priority.ALWAYS);

                    Label price = new Label(String.format("%.2f € / nuit", item.chambre.getPrix()));
                    price.setStyle("-fx-text-fill: " + ACCENT_COLOR + "; -fx-font-size: 16px; -fx-font-weight: bold;");

                    card.getChildren().addAll(details, r, price);
                    setGraphic(card);
                    setStyle("-fx-background-color: transparent; -fx-padding: 5;");
                }
            }
        });
        VBox.setVgrow(resultListView, Priority.ALWAYS);

        Button reserverButton = new Button("Confirmer la sélection");
        reserverButton.setStyle("-fx-background-color: #22c55e; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 15 30; -fx-background-radius: 50;");
        reserverButton.setAlignment(Pos.CENTER);
        reserverButton.setVisible(false);

        // LOGIC BINDING
        btnSearch.setOnAction(e -> {
            // Logic unchanged from original, just UI triggers
            try {
                LocalDate da = dateArriveePicker.getValue();
                LocalDate dd = dateDepartPicker.getValue();
                if (da == null || dd == null || da.isAfter(dd)) {
                    showAlert("Erreur", "Erreur dates"); return;
                }
                List<Chambre> chambres = ServiceManager.getChambreService().getChambresDisponiblesPourPeriode(da, dd);
                resultListView.getItems().clear();
                for (Chambre c : chambres) resultListView.getItems().add(new ChambreItem(c));
                reserverButton.setVisible(!chambres.isEmpty());
            } catch (Exception ex) { ex.printStackTrace(); }
        });

        reserverButton.setOnAction(e -> {
            ChambreItem selected = resultListView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                confirmerReservation(selected.chambre, dateArriveePicker.getValue(), dateDepartPicker.getValue(), personnesSpinner.getValue());
            }
        });

        content.getChildren().addAll(pageTitle, searchBar, resultListView, reserverButton);
        root.setCenter(content);

        Scene scene = new Scene(root, 1100, 750);
        stage.setTitle("Espace Client - Royal Suite");
        stage.setScene(scene);
        stage.show();
    }

    private Button createNavButton(String text) {
        Button b = new Button(text);
        b.setMaxWidth(Double.MAX_VALUE);
        b.setStyle(BTN_STYLE);
        b.setOnMouseEntered(e -> { if(!b.getStyle().contains("border")) b.setStyle(BTN_HOVER); });
        b.setOnMouseExited(e -> { if(!b.getStyle().contains("border")) b.setStyle(BTN_STYLE); });
        return b;
    }

    private void styleDatePicker(DatePicker dp) {
        dp.setStyle("-fx-background-color: #334155; -fx-control-inner-background: #334155; -fx-text-fill: white;");
        dp.getEditor().setStyle("-fx-background-color: #334155; -fx-text-fill: white;");
    }

    private Label createLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill: #94a3b8;");
        return l;
    }

    // --- REUSED LOGIC FROM ORIGINAL ---
    private void confirmerReservation(Chambre chambre, LocalDate dateArrivee, LocalDate dateDepart, int nombrePersonnes) {
        try {
            Client client = trouverClientParUtilisateur();
            if (client == null) return;
            Reservation reservation = new Reservation(client, chambre, dateArrivee, dateDepart, nombrePersonnes);

            Reservation createdReservation = ServiceManager.getReservationService().creerReservation(reservation);

            // Redirect to payment view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hotel/client/paiement-view.fxml"));
            Parent root = loader.load();
            PaiementController controller = loader.getController();
            controller.setReservation(createdReservation);
            Stage stage = (Stage) resultListView.getScene().getWindow();
            stage.setScene(new Scene(root));

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de la création de la réservation.");
        }
    }

    private Client trouverClientParUtilisateur() {
        try {
            String username = SessionManager.getUtilisateurConnecte().getUsername();
            List<Client> clients = ServiceManager.getClientService().getAllClients();
            for (Client c : clients) if (c.getEmail().contains(username)) return c;
            Client newClient = new Client(); newClient.setNom(username); newClient.setPrenom("Client"); newClient.setEmail(username+"@hotel.com"); newClient.setTelephone("0000");
            return ServiceManager.getClientService().ajouterClient(newClient);
        } catch(Exception e) { return null; }
    }

    private void afficherMesReservations() {
        // Get the root BorderPane
        BorderPane root = (BorderPane) stage.getScene().getRoot();

        VBox box = new VBox(10);
        box.setPadding(new Insets(40));
        box.setStyle(CONTENT_BG);
        Label t = new Label("Historique des réservations");
        t.setStyle("-fx-text-fill: white; -fx-font-size: 28px; -fx-font-weight: bold;");

        TableView<Reservation> tv = new TableView<>();
        tv.setStyle("-fx-control-inner-background: #334155; -fx-background-color: transparent;");
        tv.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Reservation, LocalDate> colDateArrivee = new TableColumn<>("Date d'arrivée");
        colDateArrivee.setCellValueFactory(new PropertyValueFactory<>("dateArrivee"));
        colDateArrivee.setPrefWidth(100);

        TableColumn<Reservation, LocalDate> colDateDepart = new TableColumn<>("Date de départ");
        colDateDepart.setCellValueFactory(new PropertyValueFactory<>("dateDepart"));
        colDateDepart.setPrefWidth(100);

        TableColumn<Reservation, String> colChambre = new TableColumn<>("Chambre");
        colChambre.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getChambre() != null ? String.valueOf(cellData.getValue().getChambre().getNumero()) : "N/A"
        ));
        colChambre.setPrefWidth(70);

        TableColumn<Reservation, String> colStatut = new TableColumn<>("Statut");
        colStatut.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getStatut() != null ? cellData.getValue().getStatut().toString() : "N/A"
        ));
        colStatut.setPrefWidth(80);

        TableColumn<Reservation, Void> colDownload = new TableColumn<>("Facture");
        colDownload.setPrefWidth(100);
        colDownload.setCellFactory(param -> new TableCell<Reservation, Void>() {
            private final Button downloadBtn = new Button("Télécharger");
            private final HBox pane = new HBox(downloadBtn);

            {
                pane.setAlignment(Pos.CENTER);
                downloadBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-size: 10px;");
                downloadBtn.setOnAction(event -> {
                    Reservation item = getTableView().getItems().get(getIndex());
                    try {
                        Facture facture = ServiceManager.getFactureService().findByReservation(item);
                        if (facture == null) {
                            facture = ServiceManager.getFactureService().genererFacture(item);
                        }
                        String fileName = "facture-reservation-" + item.getId() + ".txt";
                        try (java.io.FileWriter writer = new java.io.FileWriter(fileName)) {
                            writer.write("Facture N°: " + facture.getNumeroFacture() + "\n");
                            writer.write("Date: " + facture.getDateFacture() + "\n");
                            writer.write("Montant: " + facture.getMontant() + " €\n");
                            writer.write("Reservation: " + facture.getReservation().getId() + "\n");
                        }
                        showAlert("Facture téléchargée", "La facture a été téléchargée sous: " + fileName);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        showAlert("Erreur", "Erreur lors du téléchargement de la facture.");
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Reservation reservation = getTableView().getItems().get(getIndex());
                    // downloadBtn.setVisible(reservation.getPaiement() != null && reservation.getPaiement().getStatut() == com.hotel.enums.StatutPaiement.PAYE);
                    setGraphic(pane);
                }
            }
        });


        TableColumn<Reservation, Void> colActions = new TableColumn<>("Actions");
        colActions.setPrefWidth(100);
        colActions.setCellFactory(param -> new TableCell<Reservation, Void>() {
            private final Button cancelBtn = new Button("Annuler");
            private final HBox pane = new HBox(5, cancelBtn);

            {
                pane.setAlignment(Pos.CENTER);
                cancelBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-size: 10px;");

                cancelBtn.setOnAction(event -> {
                    Reservation item = getTableView().getItems().get(getIndex());
                    Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                    confirmAlert.setTitle("Confirmer Annulation");
                    confirmAlert.setHeaderText("Annuler la réservation #" + item.getId() + "?");
                    confirmAlert.setContentText("Cette action est irréversible.");
                    styleAlert(confirmAlert);
                    confirmAlert.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.OK) {
                            try {
                                ServiceManager.getReservationService().annulerReservation(item.getId());
                                showAlert("Annulation réussie", "La réservation #" + item.getId() + " a été annulée.");
                                // Refresh the table
                                tv.getItems().clear();
                                Client c = trouverClientParUtilisateur();
                                if (c != null) {
                                    tv.getItems().addAll(ServiceManager.getReservationService().getReservationsParClient(c.getId()));
                                }
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                showAlert("Erreur", "Erreur lors de l'annulation de la réservation.");
                            }
                        }
                    });
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Reservation reservation = getTableView().getItems().get(getIndex());
                    // Cancel button visibility and disablement
                    // More than 48 hours in advance: reservation.getDateArrivee() is after today + 2 days
                    boolean canCancel = ChronoUnit.HOURS.between(LocalDate.now().atStartOfDay(), reservation.getDateArrivee().atStartOfDay()) > 48;
                    cancelBtn.setVisible(canCancel);
                    cancelBtn.setDisable(!canCancel); // Disable if not cancellable

                    setGraphic(pane);
                }
            }
        });

        tv.getColumns().addAll(colDateArrivee, colDateDepart, colChambre, colStatut, colDownload, colActions);

        try {
            Client c = trouverClientParUtilisateur();
            if (c != null) {
                tv.getItems().setAll(FXCollections.observableArrayList(ServiceManager.getReservationService().getReservationsParClient(c.getId())));
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors du chargement des réservations.");
        }

        box.getChildren().addAll(t, tv);
        root.setCenter(box);
    }

    private void showAlert(String msg) {
        showAlert("Information", msg);
    }

    private void showAlert(String title, String content) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(content);
        styleAlert(a);
        a.show();
    }

    private void styleAlert(Alert alert) {
        DialogPane dp = alert.getDialogPane();
        dp.setStyle("-fx-background-color: #1e293b;");

        // Style content text
        try {
            ((Label) dp.lookup(".content")).setTextFill(Color.WHITE);
        } catch (Exception e) {
            // ignore if not found
        }


        // Style header panel
        try {
            VBox header = (VBox) dp.lookup(".header-panel");
            header.setStyle("-fx-background-color: #0f172a;");
            ((Label) header.getChildren().get(0)).setTextFill(Color.WHITE);
        } catch (Exception e) {
            // ignore if not found
        }
    }
}