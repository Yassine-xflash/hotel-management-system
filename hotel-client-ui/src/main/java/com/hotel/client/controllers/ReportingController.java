package com.hotel.client.controllers;

import com.hotel.interfaces.IRapportService;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.time.LocalDate;
import java.util.Map;

public class ReportingController {

    @FXML
    private DatePicker dateDebutPicker;

    @FXML
    private DatePicker dateFinPicker;

    @FXML
    private TextArea reportTextArea;

    private IRapportService rapportService;

    public ReportingController() {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            rapportService = (IRapportService) registry.lookup("RapportService");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void genererRapportOccupation() {
        LocalDate dateDebut = dateDebutPicker.getValue();
        LocalDate dateFin = dateFinPicker.getValue();
        if (dateDebut != null && dateFin != null) {
            try {
                Map<String, Object> rapport = rapportService.genererRapportOccupation(dateDebut, dateFin);
                reportTextArea.setText(rapport.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void genererRapportRevenus() {
        LocalDate dateDebut = dateDebutPicker.getValue();
        LocalDate dateFin = dateFinPicker.getValue();
        if (dateDebut != null && dateFin != null) {
            try {
                Map<String, Object> rapport = rapportService.genererRapportRevenus(dateDebut, dateFin);
                reportTextArea.setText(rapport.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
