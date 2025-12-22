package com.example.quickcommercedeliverysystemdesktop.controllers.dashboard;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class DeliveriesController {

    @FXML
    private Label deliveryInfoLabel;

    @FXML
    public void initialize() {
        deliveryInfoLabel.setText("Deliveries module ready.");
    }
}
