package com.example.quickcommercedeliverysystemdesktop.controllers.dashboard;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class EarningsController {

    @FXML
    private Label earningsInfoLabel;

    @FXML
    public void initialize() {
        earningsInfoLabel.setText("Earnings dashboard loaded.");
    }
}
