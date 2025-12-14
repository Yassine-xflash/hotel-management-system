package com.hotel.client.controllers;

import com.hotel.client.utils.ServiceManager;
import com.hotel.entities.Utilisateur;
import com.hotel.enums.RoleUtilisateur;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

public class UserManagementController {

    public VBox createUserManagementView() {
        VBox view = new VBox(10);
        view.setPadding(new Insets(15));

        Label title = new Label("Gestion des Utilisateurs (Employés et Admins)");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        Label infoLabel = new Label("⚠️ Les clients s'inscrivent via le formulaire public. " +
                "Ici vous créez uniquement des Employés et Administrateurs.");
        infoLabel.setStyle("-fx-text-fill: #e67e22; -fx-font-style: italic;");
        infoLabel.setWrapText(true);

        // Tableau des utilisateurs (simulé car on n'a pas de méthode getAllUsers)
        TableView<UtilisateurDisplay> tableView = new TableView<>();

        TableColumn<UtilisateurDisplay, String> usernameCol = new TableColumn<>("Username");
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        usernameCol.setPrefWidth(150);

        TableColumn<UtilisateurDisplay, String> roleCol = new TableColumn<>("Rôle");
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));
        roleCol.setPrefWidth(150);

        TableColumn<UtilisateurDisplay, String> actifCol = new TableColumn<>("Actif");
        actifCol.setCellValueFactory(new PropertyValueFactory<>("actif"));
        actifCol.setPrefWidth(100);

        tableView.getColumns().addAll(usernameCol, roleCol, actifCol);

        // Charger les utilisateurs connus
        List<UtilisateurDisplay> users = new ArrayList<>();
        users.add(new UtilisateurDisplay("admin", "Administrateur", "Oui"));
        users.add(new UtilisateurDisplay("employe", "Employé", "Oui"));
        tableView.setItems(FXCollections.observableArrayList(users));

        Button addButton = new Button("Ajouter Employé/Admin");
        addButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
        addButton.setOnAction(e -> showAddUserDialog(tableView));

        HBox buttons = new HBox(10, addButton);

        view.getChildren().addAll(title, infoLabel, buttons, tableView);

        return view;
    }

    private void showAddUserDialog(TableView<UtilisateurDisplay> tableView) {
        Dialog<Utilisateur> dialog = new Dialog<>();
        dialog.setTitle("Ajouter un utilisateur");
        dialog.setHeaderText("Créer un Employé ou Administrateur");

        ButtonType saveButtonType = new ButtonType("Créer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField usernameField = new TextField();
        usernameField.setPromptText("Nom d'utilisateur");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Mot de passe");

        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirmer mot de passe");

        ComboBox<RoleUtilisateur> roleCombo = new ComboBox<>();
        roleCombo.getItems().addAll(RoleUtilisateur.EMPLOYE, RoleUtilisateur.ADMIN);
        roleCombo.setValue(RoleUtilisateur.EMPLOYE);

        Label warningLabel = new Label("⚠️ Ne créez PAS de clients ici !\n" +
                "Les clients s'inscrivent via le formulaire public.");
        warningLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-style: italic;");
        warningLabel.setWrapText(true);

        grid.add(warningLabel, 0, 0, 2, 1);
        grid.add(new Label("Username:"), 0, 1);
        grid.add(usernameField, 1, 1);
        grid.add(new Label("Mot de passe:"), 0, 2);
        grid.add(passwordField, 1, 2);
        grid.add(new Label("Confirmer:"), 0, 3);
        grid.add(confirmPasswordField, 1, 3);
        grid.add(new Label("Rôle:"), 0, 4);
        grid.add(roleCombo, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    String username = usernameField.getText().trim();
                    String password = passwordField.getText();
                    String confirmPassword = confirmPasswordField.getText();

                    if (username.isEmpty() || password.isEmpty()) {
                        showAlert("Erreur", "Tous les champs sont obligatoires");
                        return null;
                    }

                    if (!password.equals(confirmPassword)) {
                        showAlert("Erreur", "Les mots de passe ne correspondent pas");
                        return null;
                    }

                    if (password.length() < 6) {
                        showAlert("Erreur", "Le mot de passe doit contenir au moins 6 caractères");
                        return null;
                    }

                    RoleUtilisateur role = roleCombo.getValue();

                    if (role == RoleUtilisateur.CLIENT) {
                        showAlert("Erreur", "Vous ne pouvez pas créer de clients ici !\n" +
                                "Les clients s'inscrivent via le formulaire public.");
                        return null;
                    }

                    Utilisateur user = ServiceManager.getAuthenticationService()
                            .creerUtilisateur(username, password, role);

                    showAlert("Succès", "Utilisateur créé avec succès !\n" +
                            "Username: " + username + "\n" +
                            "Rôle: " + role.getLibelle());

                    // Ajouter à la table
                    tableView.getItems().add(new UtilisateurDisplay(
                            username, role.getLibelle(), "Oui"));

                    return user;
                } catch (Exception e) {
                    showAlert("Erreur", "Erreur lors de la création: " + e.getMessage());
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Classe pour afficher les utilisateurs
    public static class UtilisateurDisplay {
        private String username;
        private String role;
        private String actif;

        public UtilisateurDisplay(String username, String role, String actif) {
            this.username = username;
            this.role = role;
            this.actif = actif;
        }

        public String getUsername() { return username; }
        public String getRole() { return role; }
        public String getActif() { return actif; }
    }
}
