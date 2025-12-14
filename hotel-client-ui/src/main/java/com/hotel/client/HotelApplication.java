package com.hotel.client;

import com.hotel.client.utils.ServiceManager;
import com.hotel.client.controllers.LoginController;
import javafx.application.Application;
import javafx.scene.control.Alert;
import javafx.scene.input.KeyCombination; // Added import
import javafx.stage.Stage;

public class HotelApplication extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            System.out.println("Connexion aux services distants...");
            ServiceManager.initialiser();

            LoginController controller = new LoginController();
            controller.setStage(primaryStage);
            controller.show();

            // Set the stage to maximized after showing the initial scene
            primaryStage.setMaximized(true);

        } catch (Exception e) {
            System.err.println("Erreur lors du démarrage de l'application:");
            e.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de connexion");
            alert.setHeaderText("Impossible de se connecter aux serveurs");
            alert.setContentText("Assurez-vous que les serveurs RMI et EJB sont démarrés.\n\n" +
                    e.getMessage());
            alert.showAndWait();
            System.exit(1);
        }
    }

    @Override
    public void stop() {
        System.out.println("Fermeture de l'application...");
        com.hotel.client.utils.SessionManager.deconnecter();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
