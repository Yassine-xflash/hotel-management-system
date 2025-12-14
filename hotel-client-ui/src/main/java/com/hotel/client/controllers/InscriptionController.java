package com.hotel.client.controllers;

import com.hotel.client.utils.ServiceManager;
import com.hotel.entities.Client;
import com.hotel.entities.Utilisateur;
import com.hotel.enums.RoleUtilisateur;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class InscriptionController {
    private Stage stage;

    // Styles
    private final String DARK_BG = "-fx-background-color: #0f172a;";
    private final String FORM_BG = "-fx-background-color: #1e293b; -fx-background-radius: 10;";
    private final String INPUT_STYLE = "-fx-background-color: #334155; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 8;";
    private final String LABEL_STYLE = "-fx-text-fill: #94a3b8; -fx-font-size: 12px;";
    private final String HEADER_TEXT = "-fx-text-fill: #fbbf24; -fx-font-size: 24px; -fx-font-weight: bold;";

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void show() {
        BorderPane root = new BorderPane();
        root.setStyle(DARK_BG);
        root.setPadding(new Insets(20));

        // Header
        VBox header = new VBox(5);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(0, 0, 20, 0));
        Label title = new Label("Bienvenue au Royal Suite");
        title.setStyle(HEADER_TEXT);
        Label subtitle = new Label("Complétez votre profil pour commencer");
        subtitle.setStyle("-fx-text-fill: #64748b;");
        header.getChildren().addAll(title, subtitle);
        root.setTop(header);

        // Main Form Area
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(15);
        grid.setAlignment(Pos.CENTER);
        grid.setStyle(FORM_BG);
        grid.setPadding(new Insets(30));
        grid.setMaxWidth(800);

        // Init Fields
        TextField nomField = createStyledField("Nom de famille");
        TextField prenomField = createStyledField("Prénom");
        TextField emailField = createStyledField("Adresse Email");
        TextField telephoneField = createStyledField("Téléphone");
        TextField adresseField = createStyledField("Adresse postale");
        TextField villeField = createStyledField("Ville");
        TextField codePostalField = createStyledField("Code Postal");
        TextField paysField = createStyledField("Pays");
        paysField.setText("Maroc");
        TextField cinField = createStyledField("CIN / ID");

        TextField usernameField = createStyledField("Nom d'utilisateur souhaité");
        PasswordField passwordField = createStyledPassField("Mot de passe");
        PasswordField confirmPasswordField = createStyledPassField("Confirmation");

        // Layout - 2 Columns
        addSectionTitle(grid, "1. Informations Personnelles", 0);
        grid.add(createLabel("Nom *"), 0, 1); grid.add(nomField, 0, 2);
        grid.add(createLabel("Prénom *"), 1, 1); grid.add(prenomField, 1, 2);

        grid.add(createLabel("Email *"), 0, 3); grid.add(emailField, 0, 4);
        grid.add(createLabel("Téléphone *"), 1, 3); grid.add(telephoneField, 1, 4);

        grid.add(createLabel("CIN"), 0, 5); grid.add(cinField, 0, 6);

        addSectionTitle(grid, "2. Localisation", 7);
        grid.add(createLabel("Adresse"), 0, 8, 2, 1); grid.add(adresseField, 0, 9, 2, 1);
        grid.add(createLabel("Ville"), 0, 10); grid.add(villeField, 0, 11);
        grid.add(createLabel("Pays"), 1, 10); grid.add(paysField, 1, 11);

        addSectionTitle(grid, "3. Sécurité du Compte", 12);
        grid.add(createLabel("Username *"), 0, 13); grid.add(usernameField, 0, 14);
        grid.add(createLabel("Mot de passe *"), 0, 15); grid.add(passwordField, 0, 16);
        grid.add(createLabel("Confirmer *"), 1, 15); grid.add(confirmPasswordField, 1, 16);

        // Buttons
        HBox actions = new HBox(20);
        actions.setAlignment(Pos.CENTER);
        actions.setPadding(new Insets(30, 0, 0, 0));

        Button btnCancel = new Button("Annuler");
        btnCancel.setStyle("-fx-background-color: transparent; -fx-text-fill: #94a3b8; -fx-border-color: #475569; -fx-border-radius: 5;");
        btnCancel.setPadding(new Insets(10, 30, 10, 30));
        btnCancel.setOnAction(e -> backToLogin());

        Button btnSubmit = new Button("VALIDER L'INSCRIPTION");
        btnSubmit.setStyle("-fx-background-color: #fbbf24; -fx-text-fill: #0f172a; -fx-font-weight: bold; -fx-background-radius: 5;");
        btnSubmit.setPadding(new Insets(10, 40, 10, 40));
        btnSubmit.setOnAction(e -> handleInscription(
                nomField, prenomField, emailField, telephoneField, adresseField,
                villeField, codePostalField, paysField, cinField,
                usernameField, passwordField, confirmPasswordField
        ));

        actions.getChildren().addAll(btnCancel, btnSubmit);

        VBox centerBox = new VBox(grid, actions);
        centerBox.setAlignment(Pos.CENTER);

        ScrollPane scroll = new ScrollPane(centerBox);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: #0f172a; -fx-background-color: #0f172a;");
        root.setCenter(scroll);

        Scene scene = new Scene(root, 900, 800);
        stage.setTitle("Inscription - Royal Suite");
        stage.setScene(scene);
        stage.show();
    }

    // Helper methods for UI consistency
    private TextField createStyledField(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setStyle(INPUT_STYLE);
        tf.setPrefWidth(300);
        return tf;
    }

    private PasswordField createStyledPassField(String prompt) {
        PasswordField pf = new PasswordField();
        pf.setPromptText(prompt);
        pf.setStyle(INPUT_STYLE);
        pf.setPrefWidth(300);
        return pf;
    }

    private Label createLabel(String text) {
        Label l = new Label(text);
        l.setStyle(LABEL_STYLE);
        return l;
    }

    private void addSectionTitle(GridPane grid, String title, int row) {
        Label l = new Label(title);
        l.setStyle("-fx-text-fill: #fbbf24; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 15 0 5 0;");
        grid.add(l, 0, row, 2, 1);
    }

    private void backToLogin() {
        stage.close();
        LoginController controller = new LoginController();
        Stage loginStage = new Stage();
        controller.setStage(loginStage);
        controller.show();
    }

    private void handleInscription(TextField nomField, TextField prenomField,
                                   TextField emailField, TextField telephoneField,
                                   TextField adresseField, TextField villeField,
                                   TextField codePostalField, TextField paysField,
                                   TextField cinField, TextField usernameField,
                                   PasswordField passwordField, PasswordField confirmPasswordField) {

        if (nomField.getText().trim().isEmpty() || prenomField.getText().trim().isEmpty() ||
                emailField.getText().trim().isEmpty() || telephoneField.getText().trim().isEmpty() ||
                usernameField.getText().trim().isEmpty() || passwordField.getText().isEmpty()) {
            showAlert("Formulaire incomplet", "Veuillez remplir les champs marqués d'une étoile.");
            return;
        }

        if (!passwordField.getText().equals(confirmPasswordField.getText())) {
            showAlert("Sécurité", "Les mots de passe ne correspondent pas.");
            return;
        }

        try {
            if (ServiceManager.getAuthenticationService().usernameExiste(usernameField.getText())) {
                showAlert("Erreur", "Ce nom d'utilisateur est déjà pris.");
                return;
            }

            Client client = new Client(nomField.getText().trim(), prenomField.getText().trim(),
                    emailField.getText().trim(), telephoneField.getText().trim());
            client.setAdresse(adresseField.getText());
            client.setVille(villeField.getText());
            client.setPays(paysField.getText());
            client.setCin(cinField.getText());

            ServiceManager.getClientService().ajouterClient(client);
            ServiceManager.getAuthenticationService().creerUtilisateur(
                    usernameField.getText().trim(), passwordField.getText(), RoleUtilisateur.CLIENT);

            showAlert("Succès", "Bienvenue au club ! Connectez-vous maintenant.");
            backToLogin();

        } catch (Exception e) {
            showAlert("Erreur", "Problème technique : " + e.getMessage());
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.getDialogPane().setStyle("-fx-background-color: #1e293b; -fx-text-fill: white;");
        alert.getDialogPane().lookup(".content.label").setStyle("-fx-text-fill: white;");
        alert.showAndWait();
    }
}