package com.example.quickcommercedeliverysystemdesktop.controllers.dashboard;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class OrdersController {

    @FXML
    private Label ordersInfoLabel;

    @FXML
    public void initialize() {
        ordersInfoLabel.setText("Orders module loaded successfully.");
    }
}
