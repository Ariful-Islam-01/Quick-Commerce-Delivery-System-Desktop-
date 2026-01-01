package com.example.quickcommercedeliverysystemdesktop.controllers.dashboard;

import com.example.quickcommercedeliverysystemdesktop.database.UserDAO;
import com.example.quickcommercedeliverysystemdesktop.models.User;
import com.example.quickcommercedeliverysystemdesktop.utils.ErrorHandler;
import com.example.quickcommercedeliverysystemdesktop.utils.ValidationUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;

import java.util.Optional;

/**
 * Manage Users Controller - Day 12
 * Admin panel for managing users
 */
public class ManageUsersController {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterComboBox;
    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User, Integer> userIdColumn;
    @FXML private TableColumn<User, String> nameColumn;
    @FXML private TableColumn<User, String> emailColumn;
    @FXML private TableColumn<User, String> phoneColumn;
    @FXML private TableColumn<User, String> statusColumn;
    @FXML private TableColumn<User, String> roleColumn;
    @FXML private TableColumn<User, Void> actionsColumn;
    @FXML private Label totalUsersLabel;
    @FXML private Label bannedUsersLabel;
    @FXML private Label adminUsersLabel;

    private ObservableList<User> allUsers;
    private FilteredList<User> filteredUsers;

    @FXML
    public void initialize() {
        allUsers = FXCollections.observableArrayList();
        filteredUsers = new FilteredList<>(allUsers, p -> true);

        setupTable();
        setupFilters();
        loadUsers();
    }

    private void setupTable() {
        // Set up column value factories
        userIdColumn.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getUserId()).asObject()
        );
        nameColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getName())
        );
        emailColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getEmail())
        );
        phoneColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getPhone())
        );
        statusColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().isBanned() ? "🚫 Banned" : "✅ Active")
        );
        roleColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().isAdmin() ? "👑 Admin" : "👤 User")
        );

        // Actions column with buttons
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button viewButton = new Button("👁️ View");
            private final Button editButton = new Button("✏️ Edit");
            private final Button banButton = new Button("🚫 Ban");
            private final Button deleteButton = new Button("🗑️ Delete");

            {
                viewButton.getStyleClass().add("action-button");
                editButton.getStyleClass().add("action-button");
                banButton.getStyleClass().add("action-button-warning");
                deleteButton.getStyleClass().add("action-button-danger");

                viewButton.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    viewUserDetails(user);
                });

                editButton.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    editUser(user);
                });

                banButton.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    toggleBanUser(user);
                });

                deleteButton.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    deleteUser(user);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    User user = getTableView().getItems().get(getIndex());
                    banButton.setText(user.isBanned() ? "✅ Unban" : "🚫 Ban");

                    HBox buttons = new HBox(5, viewButton, editButton, banButton, deleteButton);
                    buttons.setAlignment(Pos.CENTER);
                    setGraphic(buttons);
                }
            }
        });

        usersTable.setItems(filteredUsers);
    }

    private void setupFilters() {
        // Filter options
        filterComboBox.setItems(FXCollections.observableArrayList(
            "All Users", "Active Only", "Banned Only", "Admins Only", "Regular Users Only"
        ));
        filterComboBox.setValue("All Users");

        // Search listener
        searchField.textProperty().addListener((observable, oldValue, newValue) -> applyFilters());

        // Filter listener
        filterComboBox.valueProperty().addListener((observable, oldValue, newValue) -> applyFilters());
    }

    private void applyFilters() {
        String searchText = searchField.getText().toLowerCase();
        String filter = filterComboBox.getValue();

        filteredUsers.setPredicate(user -> {
            // Apply search filter
            boolean matchesSearch = searchText.isEmpty() ||
                user.getName().toLowerCase().contains(searchText) ||
                user.getEmail().toLowerCase().contains(searchText) ||
                (user.getPhone() != null && user.getPhone().contains(searchText));

            // Apply status filter
            boolean matchesFilter = switch (filter) {
                case "Active Only" -> !user.isBanned();
                case "Banned Only" -> user.isBanned();
                case "Admins Only" -> user.isAdmin();
                case "Regular Users Only" -> !user.isAdmin();
                default -> true;
            };

            return matchesSearch && matchesFilter;
        });

        updateStatistics();
    }

    private void loadUsers() {
        allUsers.setAll(UserDAO.getAllUsers());
        updateStatistics();
    }

    private void updateStatistics() {
        totalUsersLabel.setText("Total: " + filteredUsers.size());

        long bannedCount = allUsers.stream().filter(User::isBanned).count();
        bannedUsersLabel.setText("Banned: " + bannedCount);

        long adminCount = allUsers.stream().filter(User::isAdmin).count();
        adminUsersLabel.setText("Admins: " + adminCount);
    }

    private void viewUserDetails(User user) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("User Details");
        alert.setHeaderText("User Information");

        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        content.getChildren().addAll(
            new Label("ID: " + user.getUserId()),
            new Label("Name: " + user.getName()),
            new Label("Email: " + user.getEmail()),
            new Label("Phone: " + user.getPhone()),
            new Label("Address: " + (user.getDefaultAddress() != null ? user.getDefaultAddress() : "Not set")),
            new Label("Status: " + (user.isBanned() ? "🚫 Banned" : "✅ Active")),
            new Label("Role: " + (user.isAdmin() ? "👑 Admin" : "👤 User"))
        );

        alert.getDialogPane().setContent(content);
        alert.showAndWait();
    }

    private void editUser(User user) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit User");
        dialog.setHeaderText("Edit User Information");
        dialog.initModality(Modality.APPLICATION_MODAL);

        // Create form fields
        TextField nameField = new TextField(user.getName());
        TextField emailField = new TextField(user.getEmail());
        TextField phoneField = new TextField(user.getPhone());
        TextField addressField = new TextField(user.getDefaultAddress() != null ? user.getDefaultAddress() : "");
        CheckBox adminCheckBox = new CheckBox("Admin User");
        adminCheckBox.setSelected(user.isAdmin());

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        content.getChildren().addAll(
            new Label("Name:"), nameField,
            new Label("Email:"), emailField,
            new Label("Phone:"), phoneField,
            new Label("Address:"), addressField,
            adminCheckBox
        );

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Update user
            boolean success = UserDAO.adminUpdateUser(
                user.getUserId(),
                nameField.getText(),
                emailField.getText(),
                phoneField.getText(),
                addressField.getText()
            );

            if (success) {
                // Update admin status if changed
                if (adminCheckBox.isSelected() != user.isAdmin()) {
                    UserDAO.setAdminStatus(user.getUserId(), adminCheckBox.isSelected());
                }

                showAlert("Success", "User updated successfully", Alert.AlertType.INFORMATION);
                loadUsers();
            } else {
                showAlert("Error", "Failed to update user", Alert.AlertType.ERROR);
            }
        }
    }

    private void toggleBanUser(User user) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Action");
        confirm.setHeaderText(user.isBanned() ? "Unban User" : "Ban User");
        confirm.setContentText(
            user.isBanned()
                ? "Are you sure you want to unban " + user.getName() + "?"
                : "Are you sure you want to ban " + user.getName() + "?"
        );

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = UserDAO.setBanStatus(user.getUserId(), !user.isBanned());

            if (success) {
                showAlert("Success",
                    user.isBanned() ? "User unbanned successfully" : "User banned successfully",
                    Alert.AlertType.INFORMATION);
                loadUsers();
            } else {
                showAlert("Error", "Failed to update user status", Alert.AlertType.ERROR);
            }
        }
    }

    private void deleteUser(User user) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete User");
        confirm.setContentText("Are you sure you want to permanently delete " + user.getName() + "?\n" +
                              "This action cannot be undone and will also delete all their orders and deliveries.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = UserDAO.deleteUser(user.getUserId());

            if (success) {
                showAlert("Success", "User deleted successfully", Alert.AlertType.INFORMATION);
                loadUsers();
            } else {
                showAlert("Error", "Failed to delete user", Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void handleRefresh() {
        loadUsers();
        searchField.clear();
        filterComboBox.setValue("All Users");
    }

    @FXML
    private void handleAddUser() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add New User");
        dialog.setHeaderText("Create New User Account");
        dialog.initModality(Modality.APPLICATION_MODAL);

        // Create form fields
        TextField nameField = new TextField();
        nameField.setPromptText("Full Name");

        TextField emailField = new TextField();
        emailField.setPromptText("Email Address");

        TextField phoneField = new TextField();
        phoneField.setPromptText("Phone Number");

        TextField addressField = new TextField();
        addressField.setPromptText("Default Address (Optional)");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm Password");

        CheckBox adminCheckBox = new CheckBox("Create as Admin User");

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        content.getChildren().addAll(
            new Label("Name:"), nameField,
            new Label("Email:"), emailField,
            new Label("Phone:"), phoneField,
            new Label("Address:"), addressField,
            new Label("Password:"), passwordField,
            new Label("Confirm Password:"), confirmPasswordField,
            adminCheckBox
        );

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Validate inputs
                String name = nameField.getText().trim();
                String email = emailField.getText().trim();
                String phone = phoneField.getText().trim();
                String address = addressField.getText().trim();
                String password = passwordField.getText();
                String confirmPassword = confirmPasswordField.getText();

                // Validate required fields
                if (!ValidationUtil.isNotEmpty(name)) {
                    ValidationUtil.showAlert("Validation Error", "Name is required", Alert.AlertType.ERROR);
                    return;
                }

                if (!ValidationUtil.isValidEmail(email)) {
                    ValidationUtil.showAlert("Validation Error", "Please enter a valid email address", Alert.AlertType.ERROR);
                    return;
                }

                if (!phone.isEmpty() && !ValidationUtil.isValidPhone(phone)) {
                    ValidationUtil.showAlert("Validation Error", "Please enter a valid phone number (10-15 digits)", Alert.AlertType.ERROR);
                    return;
                }

                if (!ValidationUtil.isValidPassword(password)) {
                    ValidationUtil.showAlert("Validation Error", "Password must be at least 6 characters", Alert.AlertType.ERROR);
                    return;
                }

                if (!password.equals(confirmPassword)) {
                    ValidationUtil.showAlert("Validation Error", "Passwords do not match", Alert.AlertType.ERROR);
                    return;
                }

                // Create user account
                boolean success = com.example.quickcommercedeliverysystemdesktop.database.UserDAO.register(
                    name, email, phone, password
                );

                if (success) {
                    // If admin checkbox is selected, set admin status
                    if (adminCheckBox.isSelected()) {
                        // Get the newly created user to set admin status
                        java.util.List<User> users = UserDAO.getAllUsers();
                        User newUser = users.stream()
                            .filter(u -> u.getEmail().equals(email))
                            .findFirst()
                            .orElse(null);

                        if (newUser != null) {
                            UserDAO.setAdminStatus(newUser.getUserId(), true);
                        }
                    }

                    showAlert("Success", "User account created successfully", Alert.AlertType.INFORMATION);
                    loadUsers();
                } else {
                    showAlert("Error", "Failed to create user. Email may already exist.", Alert.AlertType.ERROR);
                }
            } catch (Exception e) {
                ErrorHandler.handleDatabaseException(e, "creating user");
                ValidationUtil.showAlert("Error", "Failed to create user. Please try again.", Alert.AlertType.ERROR);
            }
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

