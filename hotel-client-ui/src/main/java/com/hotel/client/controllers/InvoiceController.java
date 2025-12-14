package com.hotel.client.controllers;

import com.hotel.entities.Paiement;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.fxml.FXMLLoader;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;

public class InvoiceController {

    @FXML
    private Label paiementIdLabel;

    @FXML
    private Label reservationIdLabel;

    @FXML
    private Label chambreLabel;

    @FXML
    private Label montantLabel;

    @FXML
    private Label methodeLabel;

    @FXML
    private Label dateLabel;

    private Paiement paiement;

    public void setPaiement(Paiement paiement) {
        this.paiement = paiement;
        paiementIdLabel.setText(String.valueOf(paiement.getId()));
        reservationIdLabel.setText(String.valueOf(paiement.getReservation().getId()));
        chambreLabel.setText(paiement.getReservation().getChambre().getNumero());
        montantLabel.setText(String.format("%.2f €", paiement.getMontant()));
        methodeLabel.setText(paiement.getMethodePaiement());
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        dateLabel.setText(sdf.format(paiement.getDatePaiement()));
    }

    @FXML
    private void handleDownload() {
        try {
            String fileName = "facture-" + paiement.getId() + ".txt";
            FileWriter writer = new FileWriter(fileName);
            writer.write("Facture de Paiement\n");
            writer.write("====================\n");
            writer.write("ID de Paiement: " + paiement.getId() + "\n");
            writer.write("ID de Réservation: " + paiement.getReservation().getId() + "\n");
            writer.write("Chambre: " + paiement.getReservation().getChambre().getNumero() + "\n");
            writer.write("Montant Payé: " + String.format("%.2f €", paiement.getMontant()) + "\n");
            writer.write("Méthode de Paiement: " + paiement.getMethodePaiement() + "\n");
            writer.write("Date de Paiement: " + dateLabel.getText() + "\n");
            writer.close();
            
            // You can add a label to confirm download
            System.out.println("Facture téléchargée: " + fileName);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBackToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hotel/client/client-dashboard-view.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) dateLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}