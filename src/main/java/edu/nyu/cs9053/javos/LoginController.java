package edu.nyu.cs9053.javos;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {
    @FXML
    private TextField usernameField;
    
    @FXML
    private PasswordField passwordField;
    
    @FXML
    private Label errorLabel;
    
    private static final String VALID_USERNAME = "admin";
    private static final String VALID_PASSWORD = "password";
    
    private Runnable onLoginSuccess;
    
    public void setOnLoginSuccess(Runnable callback) {
        this.onLoginSuccess = callback;
    }
    
    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        
        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please enter both username and password");
            return;
        }
        
        if (username.equals(VALID_USERNAME) && password.equals(VALID_PASSWORD)) {
            if (onLoginSuccess != null) {
                onLoginSuccess.run();
            }
        } else {
            errorLabel.setText("Invalid username or password");
        }
    }
} 