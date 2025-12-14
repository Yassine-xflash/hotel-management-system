package com.hotel.client.controllers;

import com.hotel.client.utils.SessionManager;
import com.hotel.entities.Reservation;
import com.hotel.client.utils.ServiceManager;
import com.hotel.enums.StatutPaiement;
import com.hotel.entities.Paiement;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDateTime;

public class PaiementController {

    @FXML
    private BorderPane rootPane;

    @FXML
    private Label userMsgLabel;

    @FXML
    private Label montantLabel;

    @FXML
    private TextField cardNumberField;

    @FXML
    private TextField expiryDateField;

    @FXML
    private TextField cvvField;

    @FXML
    private Button payWithCardButton;

    @FXML
    private Button payWithPayPalButton;

    @FXML
    private Label statusLabel;

    private Reservation reservation;

    @FXML
    public void initialize() {
        userMsgLabel.setText("Bonjour, " + SessionManager.getUtilisateurConnecte().getUsername());
    }

    public void setReservation(Reservation reservation) {
        this.reservation = reservation;
        montantLabel.setText(String.format("%.2f €", reservation.getChambre().getPrix()));
    }

    @FXML
    private void handlePayWithCard() {
        if (validateCardDetails()) {
            processPayment(com.hotel.enums.TypePaiement.CARTE_CREDIT);
        } else {
            statusLabel.setText("Détails de la carte invalides.");
        }
    }

    @FXML
    private void handlePayWithPayPal() {
        processPayment(com.hotel.enums.TypePaiement.VIREMENT);
    }

    private boolean validateCardDetails() {
        return !cardNumberField.getText().isEmpty() &&
               !expiryDateField.getText().isEmpty() &&
               !cvvField.getText().isEmpty();
    }

    private void processPayment(com.hotel.enums.TypePaiement methode) {
        try {
            Paiement paiement = new Paiement();
            paiement.setReservation(reservation);
            paiement.setMontant(reservation.getChambre().getPrix());
            paiement.setDatePaiement(LocalDateTime.now());
            paiement.setMethodePaiement(methode);
            paiement.setStatut(StatutPaiement.PAYE);

            Paiement savedPaiement = ServiceManager.getPaiementService().creerPaiement(paiement);
            reservation.setPaiement(savedPaiement);
            
            statusLabel.setText("Paiement effectué avec succès!");
            
            // Redirect to invoice view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hotel/client/invoice-view.fxml"));
            Parent root = loader.load();
            InvoiceController controller = loader.getController();
            controller.setPaiement(savedPaiement);
            Stage stage = (Stage) rootPane.getScene().getWindow();
            stage.setScene(new Scene(root));

        } catch (Exception e) {
            statusLabel.setText("Erreur de paiement: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

