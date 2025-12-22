package com.example.quickcommercedeliverysystemdesktop.controllers.dashboard;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class HomeController {

    @FXML
    private Label welcomeLabel;

    @FXML
    public void initialize() {
        welcomeLabel.setText("Welcome to Quick Commerce Dashboard");
    }
}
