package com.hotel.client.controllers;

import com.hotel.client.utils.ServiceManager;
import com.hotel.client.utils.SessionManager;
import com.hotel.entities.Utilisateur;
import com.hotel.enums.RoleUtilisateur;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class LoginController {
    private Stage stage;
    private TextField usernameField;
    private PasswordField passwordField;

    // Theme Colors
    private final String BG_COLOR = "-fx-background-color: #0f172a;"; // Deep Slate
    private final String CARD_COLOR = "-fx-background-color: #1e293b; -fx-background-radius: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 10, 0, 0, 5);";
    private final String TEXT_FILL = "-fx-text-fill: #f1f5f9;";
    private final String GOLD_BUTTON = "-fx-background-color: #fbbf24; -fx-text-fill: #0f172a; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand;";
    private final String OUTLINE_BUTTON = "-fx-background-color: transparent; -fx-border-color: #94a3b8; -fx-border-radius: 8; -fx-text-fill: #94a3b8; -fx-cursor: hand;";
    private final String INPUT_STYLE = "-fx-background-color: #334155; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 10;";

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void show() {
        StackPane mainContainer = new StackPane();
        mainContainer.setStyle(BG_COLOR);

        // Card Container
        VBox card = new VBox(25);
        card.setAlignment(Pos.CENTER);
        card.setMaxWidth(420);
        card.setMaxHeight(550);
        card.setPadding(new Insets(40));
        card.setStyle(CARD_COLOR);

        // Header
        Label iconLabel = new Label("🏨");
        iconLabel.setStyle("-fx-font-size: 48px;");

        Label titleLabel = new Label("ROYAL SUITE");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        titleLabel.setStyle("-fx-text-fill: #fbbf24; -fx-letter-spacing: 2;"); // Gold Title

        Label subtitleLabel = new Label("Accédez à votre espace");
        subtitleLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        subtitleLabel.setStyle("-fx-text-fill: #94a3b8;");

        // Form
        VBox fieldsBox = new VBox(15);
        fieldsBox.setAlignment(Pos.CENTER_LEFT);

        Label userLbl = new Label("UTILISATEUR");
        userLbl.setStyle("-fx-text-fill: #64748b; -fx-font-size: 10px; -fx-font-weight: bold;");
        usernameField = new TextField();
        usernameField.setPromptText("ex: admin");
        usernameField.setStyle(INPUT_STYLE);

        Label passLbl = new Label("MOT DE PASSE");
        passLbl.setStyle("-fx-text-fill: #64748b; -fx-font-size: 10px; -fx-font-weight: bold;");
        passwordField = new PasswordField();
        passwordField.setPromptText("••••••");
        passwordField.setStyle(INPUT_STYLE);

        fieldsBox.getChildren().addAll(userLbl, usernameField, passLbl, passwordField);

        // Buttons
        Button loginButton = new Button("SE CONNECTER");
        loginButton.setPrefWidth(340);
        loginButton.setPrefHeight(45);
        loginButton.setStyle(GOLD_BUTTON);
        loginButton.setOnAction(e -> handleLogin());

        // Hover effect styling
        loginButton.setOnMouseEntered(e -> loginButton.setStyle(GOLD_BUTTON + "-fx-background-color: #f59e0b;"));
        loginButton.setOnMouseExited(e -> loginButton.setStyle(GOLD_BUTTON));

        Button inscriptionButton = new Button("Créer un nouveau compte");
        inscriptionButton.setPrefWidth(340);
        inscriptionButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #fbbf24; -fx-underline: true; -fx-cursor: hand;");
        inscriptionButton.setOnAction(e -> {
            stage.close();
            InscriptionController controller = new InscriptionController();
            Stage inscriptionStage = new Stage();
            controller.setStage(inscriptionStage);
            controller.show();
        });

        Button exitButton = new Button("Quitter l'application");
        exitButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #ef4444; -fx-font-size: 11px; -fx-cursor: hand;");
        exitButton.setOnAction(e -> System.exit(0));

        // Footer helper
        Label helpLabel = new Label("Comptes démo : admin/admin123 | employe/employe123");
        helpLabel.setStyle("-fx-text-fill: #475569; -fx-font-size: 10px;");

        card.getChildren().addAll(iconLabel, titleLabel, subtitleLabel, fieldsBox, loginButton, inscriptionButton, helpLabel, exitButton);
        mainContainer.getChildren().add(card);

        Scene scene = new Scene(mainContainer, 900, 700);
        stage.setTitle("Login - Royal Suite Hotel");
        stage.setScene(scene);
        stage.show();

        usernameField.requestFocus();
        passwordField.setOnAction(e -> handleLogin());
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Attention", "Champs requis manquants", Alert.AlertType.WARNING);
            return;
        }

        try {
            Utilisateur utilisateur = ServiceManager.getAuthenticationService()
                    .authentifier(username, password);

            if (utilisateur != null) {
                SessionManager.setUtilisateurConnecte(utilisateur);
                stage.close();
                openDashboard(utilisateur.getRole());
            } else {
                showAlert("Échec", "Identifiants invalides", Alert.AlertType.ERROR);
                passwordField.clear();
            }
        } catch (Exception e) {
            showAlert("Erreur Système", e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void openDashboard(RoleUtilisateur role) {
        try {
            Stage dashboardStage = new Stage();
            dashboardStage.setMaximized(true); // Add this line
            if (role == RoleUtilisateur.ADMIN || role == RoleUtilisateur.EMPLOYE) {
                EmployeDashboardController controller = new EmployeDashboardController();
                controller.setStage(dashboardStage);
                controller.show();
            } else if (role == RoleUtilisateur.CLIENT) {
                ClientDashboardController controller = new ClientDashboardController();
                controller.setStage(dashboardStage);
                controller.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #1e293b; -fx-text-fill: white;");
        dialogPane.lookup(".content.label").setStyle("-fx-text-fill: white;");
        alert.showAndWait();
    }
}