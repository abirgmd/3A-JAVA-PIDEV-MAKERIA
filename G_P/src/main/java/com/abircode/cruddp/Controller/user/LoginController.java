package com.abircode.cruddp.Controller.user;

import com.abircode.cruddp.entities.User;
import com.abircode.cruddp.services.user.LoginAttemptManager;
import com.abircode.cruddp.services.user.UserService;
import com.abircode.cruddp.utils.GoogleAuthService;
import com.abircode.cruddp.utils.SessionManager;
import com.abircode.cruddp.utils.VerificationCodeManager;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Pair;

import java.io.IOException;
import java.sql.SQLException;
import java.util.function.UnaryOperator;
import java.util.prefs.Preferences;

public class LoginController {
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private CheckBox rememberMeCheckbox;
    @FXML private Button loginButton;
    @FXML private Button registerButton;
    @FXML private HBox mainContainer;
    @FXML private Label titleLabel;

    private final UserService userService = new UserService();
    private Stage loadingStage;

    @FXML
    public void initialize() {

        setupUIEffects();
        loadRememberedCredentials();
        emailField.requestFocus();
    }
    @FXML
    private void handleKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            ActionEvent actionEvent = new ActionEvent(event.getSource(), event.getTarget());
            attemptLogin(actionEvent);
        }
    }
    @FXML
    private void handleLogin(ActionEvent event) {
        attemptLogin(event);
    }

    @FXML
    private void handleGoogleLogin(ActionEvent event) {
        try {
            // Create a WebView to show Google's login page
            WebView webView = new WebView();
            WebEngine webEngine = webView.getEngine();

            // Load the Google authorization URL
            String authUrl = GoogleAuthService.getAuthorizationUrl();
            webEngine.load(authUrl);

            // Create a new stage for the WebView
            Stage webStage = new Stage();
            webStage.setScene(new Scene(webView, 600, 800));
            webStage.setTitle("Sign in with Google");
            webStage.show();

            // Listen for URL changes to capture the authorization code
            webEngine.locationProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null && newValue.contains("code=")) {
                    String code = newValue.split("code=")[1].split("&")[0];
                    webStage.close();

                    showLoadingScreen("Authenticating with Google...");

                    Task<User> googleAuthTask = new Task<User>() {
                        @Override
                        protected User call() throws Exception {
                            return GoogleAuthService.authenticateWithGoogle(code, userService);
                        }
                    };

                    googleAuthTask.setOnSucceeded(e -> {
                        hideLoadingScreen();
                        User googleUser = googleAuthTask.getValue();
                        SessionManager.setCurrentUser(googleUser);
                        navigateBasedOnRole(googleUser, event);
                    });

                    googleAuthTask.setOnFailed(e -> {
                        hideLoadingScreen();
                        showError("Google authentication failed: " + googleAuthTask.getException().getMessage());
                    });

                    new Thread(googleAuthTask).start();
                }
            });
        } catch (Exception e) {
            showError("Google login failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private void attemptLogin(ActionEvent event) {
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Please enter both email and password");
            return;
        }

        LoginAttemptManager attemptManager = LoginAttemptManager.getInstance();

        // Vérifier si bloqué
        if (attemptManager.isBlocked(email)) {
            long remainingTime = attemptManager.getRemainingBlockTime(email);
            showError("Too many failed attempts. Try again in " + remainingTime + " minutes.");
            return;
        }

        try {
            User user = userService.login(email, password);
            if (user != null) {
                if (user.isBlok()) {
                    showError("Your account is blocked. Please contact support.");
                } else {
                    handleSuccessfulLogin(user, email, password, event);
                }
            } else {
                int attemptsLeft = attemptManager.getRemainingAttempts(email);
                if (attemptsLeft > 0) {
                    showError("Invalid email or password. " + attemptsLeft + " attempts left.");
                } else {
                    showError("Account temporarily blocked. Try again in 10 minutes.");
                }
            }
        } catch (SQLException e) {
            showError("Login error: " + e.getMessage());
            e.printStackTrace();
        }
    }    private void rememberCredentials(String email, String password) {
        Preferences prefs = Preferences.userNodeForPackage(LoginController.class);
        prefs.put("rememberedEmail", email);
        prefs.put("rememberedPassword", password);
    }
    private void handleSuccessfulLogin(User user, String email, String password, ActionEvent event) {
        SessionManager.setCurrentUser(user);

        if (rememberMeCheckbox.isSelected()) {
            rememberCredentials(email, password);
        } else {
            clearRememberedCredentials();
        }

        navigateBasedOnRole(user, event);
    }
    private void navigateBasedOnRole(User user, ActionEvent event) {
        try {
            String fxmlPath;

            if (user.getRoles().contains("ROLE_ADMIN") || user.getRoles().contains("ROLE_ARTIST")) {
                fxmlPath = "/fxml/produits.fxml";
            } else {
                fxmlPath = "/fxml/MakeriaF.fxml";
            }


            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Stage newStage = new Stage();
            newStage.setScene(new Scene(root));
            newStage.setTitle(user.getRoles().contains("ROLE_ADMIN") ? "Admin Dashboard" : "User Dashboard");
            newStage.setFullScreen(true);
            newStage.setFullScreenExitHint("Press ESC to exit full screen");
            newStage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);

            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.close();

            newStage.show();
        } catch (IOException e) {
            showError("Failed to load dashboard");
            e.printStackTrace();
        }
    }
    private void clearRememberedCredentials() {
        Preferences prefs = Preferences.userNodeForPackage(LoginController.class);
        prefs.remove("rememberedEmail");
        prefs.remove("rememberedPassword");
    }
    @FXML
    private void goToRegister(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/register.fxml"));
            Parent root = loader.load();

            Stage registerStage = new Stage();
            registerStage.setScene(new Scene(root));
            registerStage.setTitle("Register");
            registerStage.setFullScreen(true);
            registerStage.setFullScreenExitHint("Press ESC to exit full screen");
            registerStage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);

            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.close();

            registerStage.show();
        } catch (IOException e) {
            showError("Failed to load registration form");
            e.printStackTrace();
        }
    }

    private void setupUIEffects() {
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.2));
        shadow.setRadius(10);
        shadow.setSpread(0.05);
        mainContainer.setEffect(shadow);

        // ... [rest of your existing UI setup code]
    }

    private void loadRememberedCredentials() {
        Preferences prefs = Preferences.userNodeForPackage(LoginController.class);
        String rememberedEmail = prefs.get("rememberedEmail", null);
        String rememberedPassword = prefs.get("rememberedPassword", null);

        if (rememberedEmail != null && rememberedPassword != null) {
            emailField.setText(rememberedEmail);
            passwordField.setText(rememberedPassword);
            rememberMeCheckbox.setSelected(true);
        }
    }

    // ... [keep all your existing methods until handleForgotPassword]
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
    }

    @FXML
    private void handleForgotPassword(ActionEvent event) {
        // Email Input Dialog
        Dialog<String> emailDialog = new Dialog<>();
        emailDialog.setTitle("Password Reset");
        emailDialog.setHeaderText("Enter your registered email address");

        // Setup dialog content
        VBox emailContent = new VBox(15);
        emailContent.setStyle(
                "-fx-background-color: #ffffff; " +
                        "-fx-padding: 20; " +
                        "-fx-border-radius: 15; " +
                        "-fx-background-radius: 15; " +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);" +
                        "-fx-font-family: 'Comic Sans MS';"
        );

        Label emailLabel = new Label("We'll send a verification code to your email:");
        emailLabel.setStyle(
                "-fx-text-fill: #333333; " +
                        "-fx-font-size: 14px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-font-family: 'Comic Sans MS';"
        );

        TextField emailInput = new TextField();
        emailInput.setPromptText("your@email.com");
        emailInput.setStyle(
                "-fx-background-color: #f2f2f2; " +
                        "-fx-text-fill: #333333; " +
                        "-fx-prompt-text-fill: #999999; " +
                        "-fx-background-radius: 8; " +
                        "-fx-border-color: transparent; " +
                        "-fx-font-size: 13px; " +
                        "-fx-font-family: 'Comic Sans MS';"
        );

        ProgressIndicator emailProgress = new ProgressIndicator();
        emailProgress.setVisible(false);
        emailProgress.setMaxSize(30, 30);
        // Correction ici : une couleur simple au lieu d'un dégradé
        emailProgress.setStyle(
                "-fx-accent: #8a2be2; " +
                        "-fx-font-family: 'Comic Sans MS';"
        );

        emailContent.getChildren().addAll(emailLabel, emailInput, emailProgress);
        emailDialog.getDialogPane().setContent(emailContent);

        // Buttons
        ButtonType sendButtonType = new ButtonType("Send Code", ButtonBar.ButtonData.OK_DONE);
        emailDialog.getDialogPane().getButtonTypes().addAll(sendButtonType, ButtonType.CANCEL);

        Node sendButton = emailDialog.getDialogPane().lookupButton(sendButtonType);
        sendButton.setDisable(true);
        sendButton.setStyle(
                "-fx-background-color: linear-gradient(to right, #8a2be2, #da70d6); " +
                        "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-background-radius: 10; " +
                        "-fx-padding: 8 20; " +
                        "-fx-font-family: 'Comic Sans MS';"
        );

        Node cancelButton = emailDialog.getDialogPane().lookupButton(ButtonType.CANCEL);
        if (cancelButton != null) {
            cancelButton.setStyle(
                    "-fx-background-color: transparent; " +
                            "-fx-text-fill: #999999; " +
                            "-fx-font-weight: normal; " +
                            "-fx-font-size: 13px; " +
                            "-fx-font-family: 'Comic Sans MS';"
            );
        }

        emailInput.textProperty().addListener((obs, oldVal, newVal) -> {
            sendButton.setDisable(newVal.trim().isEmpty());
        });

        emailDialog.setResultConverter(buttonType -> {
            if (buttonType == sendButtonType) {
                return emailInput.getText().trim();
            }
            return null;
        });

        emailDialog.showAndWait().ifPresent(email -> {
            emailProgress.setVisible(true);
            sendButton.setDisable(true);

            Task<Boolean> emailCheckTask = new Task<Boolean>() {
                @Override
                protected Boolean call() throws Exception {
                    return userService.emailExists(email);
                }
            };

            emailCheckTask.setOnSucceeded(e -> {
                if (emailCheckTask.getValue()) {
                    VerificationCodeManager.generateAndStoreCode(email)
                            .thenAcceptAsync(code -> {
                                Platform.runLater(() -> {
                                    emailProgress.setVisible(false);
                                    showVerificationDialog(email);
                                });
                            })
                            .exceptionally(ex -> {
                                Platform.runLater(() -> {
                                    emailProgress.setVisible(false);
                                    sendButton.setDisable(false);
                                    showError("Failed to send verification code: " + ex.getMessage());
                                });
                                return null;
                            });
                } else {
                    Platform.runLater(() -> {
                        emailProgress.setVisible(false);
                        sendButton.setDisable(false);
                        showError("No account found with this email");
                    });
                }
            });

            emailCheckTask.setOnFailed(e -> {
                Platform.runLater(() -> {
                    emailProgress.setVisible(false);
                    sendButton.setDisable(false);
                    showError("Database error: " + emailCheckTask.getException().getMessage());
                });
            });

            new Thread(emailCheckTask).start();
        });
    }



    private void showVerificationDialog(String email) {
        Dialog<String> verificationDialog = new Dialog<>();
        verificationDialog.setTitle("Verify Your Email");
        verificationDialog.setHeaderText("Enter the 6-digit code sent to " + email);

        UnaryOperator<TextFormatter.Change> digitFilter = change -> {
            String newText = change.getControlNewText();
            if (newText.matches("\\d{0,6}")) {
                return change;
            } else {
                return null;
            }
        };

        // Setup dialog content
        VBox verifyContent = new VBox(15);
        verifyContent.setStyle(
                "-fx-background-color: #ffffff; " + // Fond blanc
                        "-fx-padding: 20; " +
                        "-fx-border-radius: 15; " +
                        "-fx-background-radius: 15; " +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);" // Effet d'ombre
        );

        TextField codeField = new TextField();
        codeField.setPromptText("123456");
        TextFormatter<String> textFormatter = new TextFormatter<>(digitFilter);
        codeField.setTextFormatter(textFormatter);
        codeField.setStyle(
                "-fx-background-color: #f2f2f2; " +
                        "-fx-text-fill: #333333; " +
                        "-fx-prompt-text-fill: #999999; " +
                        "-fx-background-radius: 8; " +
                        "-fx-border-color: transparent; " +
                        "-fx-font-size: 13px; " +
                        "-fx-font-family: 'Comic Sans MS';" // Police Comic Sans MS
        );

        // Progress indicator
        ProgressIndicator verifyProgress = new ProgressIndicator();
        verifyProgress.setVisible(false);
        verifyProgress.setMaxSize(30, 30);
        // Correction ici : utiliser -fx-accent au lieu de -fx-progress-color
        verifyProgress.setStyle("-fx-accent: #8a2be2;");

        // Resend code link
        Hyperlink resendLink = new Hyperlink("Resend code");
        resendLink.setOnAction(e -> {
            verifyProgress.setVisible(true);
            VerificationCodeManager.generateAndStoreCode(email)
                    .thenAcceptAsync(code -> {
                        Platform.runLater(() -> {
                            verifyProgress.setVisible(false);
                            showAlert(Alert.AlertType.INFORMATION, "Code Resent", "New verification code sent");
                        });
                    })
                    .exceptionally(ex -> {
                        Platform.runLater(() -> {
                            verifyProgress.setVisible(false);
                            showAlert(Alert.AlertType.ERROR, "Error", "Failed to resend code");
                        });
                        return null;
                    });
        });

        // Style for resend link (mauve and Comic Sans MS)
        resendLink.setStyle(
                "-fx-text-fill: #8A2BE2; " + // Mauve
                        "-fx-font-family: 'Comic Sans MS'; " + // Police Comic Sans MS
                        "-fx-font-size: 14px; " + // Taille de la police
                        "-fx-font-weight: bold;" // Texte en gras
        );

        verifyContent.getChildren().addAll(codeField, verifyProgress, resendLink);
        verificationDialog.getDialogPane().setContent(verifyContent);

        // Configure buttons
        ButtonType verifyButtonType = new ButtonType("Verify", ButtonBar.ButtonData.OK_DONE);
        verificationDialog.getDialogPane().getButtonTypes().addAll(verifyButtonType, ButtonType.CANCEL);

        // Enable/disable verify button
        Node verifyButton = verificationDialog.getDialogPane().lookupButton(verifyButtonType);
        verifyButton.setDisable(true);
        verifyButton.setStyle(
                "-fx-background-color: linear-gradient(to right, #8a2be2, #da70d6); " +
                        "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-background-radius: 10; " +
                        "-fx-padding: 8 20; " +
                        "-fx-font-family: 'Comic Sans MS';" // Police Comic Sans MS
        );

        Node cancelButton = verificationDialog.getDialogPane().lookupButton(ButtonType.CANCEL);
        if (cancelButton != null) {
            cancelButton.setStyle(
                    "-fx-background-color: transparent; " +
                            "-fx-text-fill: #999999; " +
                            "-fx-font-weight: normal; " +
                            "-fx-font-size: 13px; " +
                            "-fx-font-family: 'Comic Sans MS';" // Police Comic Sans MS
            );
        }

        codeField.textProperty().addListener((obs, oldVal, newVal) -> {
            verifyButton.setDisable(newVal.trim().length() != 6);
        });

        // Set result converter
        verificationDialog.setResultConverter(buttonType -> {
            if (buttonType == verifyButtonType) {
                return codeField.getText().trim();
            }
            return null;
        });

        // Show dialog and handle response
        verificationDialog.showAndWait().ifPresent(code -> {
            verifyProgress.setVisible(true);
            verifyButton.setDisable(true);

            Task<Boolean> verifyTask = new Task<Boolean>() {
                @Override
                protected Boolean call() throws Exception {
                    return VerificationCodeManager.verifyCode(email, code);
                }
            };

            verifyTask.setOnSucceeded(e -> {
                verifyProgress.setVisible(false);
                if (verifyTask.getValue()) {
                    showPasswordResetDialog(email);
                } else {
                    verifyButton.setDisable(false);
                    showError("Invalid verification code");
                }
            });

            verifyTask.setOnFailed(e -> {
                verifyProgress.setVisible(false);
                verifyButton.setDisable(false);
                showError("Verification error: " + verifyTask.getException().getMessage());
            });

            new Thread(verifyTask).start();
        });
    }


    private void showPasswordResetDialog(String email) {
        Dialog<Pair<String, String>> passwordDialog = new Dialog<>();
        passwordDialog.setTitle("🔒 Create New Password");
        passwordDialog.setHeaderText("Please enter your new password");

        // Style the dialog pane
        DialogPane dialogPane = passwordDialog.getDialogPane();
        dialogPane.setStyle(
                "-fx-background-color: #faf7fd; " + // Fond mauve très clair
                        "-fx-border-radius: 15; " +
                        "-fx-background-radius: 15; " +
                        "-fx-padding: 20;"
        );

        VBox resetContent = new VBox(15);
        resetContent.setAlignment(Pos.CENTER_LEFT);
        resetContent.setStyle(
                "-fx-background-color: #ffffff;" +
                        "-fx-background-radius: 10;" +
                        "-fx-padding: 20;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(138,43,226,0.2), 10, 0, 0, 5);"
        );

        Label newPasswordLabel = new Label("New Password:");
        newPasswordLabel.setStyle("-fx-text-fill: black; -fx-font-weight: bold;");

        PasswordField newPasswordField = new PasswordField();
        newPasswordField.setPromptText("Enter new password");
        newPasswordField.setStyle(
                "-fx-background-color: #f2eaff;" +
                        "-fx-text-fill: black;" +
                        "-fx-prompt-text-fill: #888888;" +
                        "-fx-background-radius: 8;" +
                        "-fx-font-size: 13px;"
        );

        Label strengthLabel = new Label("Password Strength:");
        strengthLabel.setStyle("-fx-text-fill: black; -fx-font-weight: bold;");

        ProgressBar strengthBar = new ProgressBar(0);
        strengthBar.setPrefWidth(200);
        strengthBar.setStyle("-fx-accent: #8a2be2;");

        Label confirmPasswordLabel = new Label("Confirm Password:");
        confirmPasswordLabel.setStyle("-fx-text-fill: black; -fx-font-weight: bold;");

        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm your password");
        confirmPasswordField.setStyle(
                "-fx-background-color: #f2eaff;" +
                        "-fx-text-fill: black;" +
                        "-fx-prompt-text-fill: #888888;" +
                        "-fx-background-radius: 8;" +
                        "-fx-font-size: 13px;"
        );

        ProgressIndicator resetProgress = new ProgressIndicator();
        resetProgress.setVisible(false);
        resetProgress.setMaxSize(30, 30);
        resetProgress.setStyle("-fx-accent: #8a2be2;");

        newPasswordField.textProperty().addListener((obs, oldVal, newVal) -> {
            double strength = calculatePasswordStrength(newVal);
            strengthBar.setProgress(strength);
            strengthBar.setStyle(getStrengthColor(strength));
        });

        resetContent.getChildren().addAll(
                newPasswordLabel, newPasswordField,
                strengthLabel, strengthBar,
                confirmPasswordLabel, confirmPasswordField,
                resetProgress
        );
        passwordDialog.getDialogPane().setContent(resetContent);

        // Buttons
        ButtonType resetButtonType = new ButtonType("Reset Password", ButtonBar.ButtonData.OK_DONE);
        dialogPane.getButtonTypes().addAll(resetButtonType, ButtonType.CANCEL);

        Node resetButton = dialogPane.lookupButton(resetButtonType);
        resetButton.setDisable(true);

        // STYLE du bouton mauve arrondi
        resetButton.setStyle(
                "-fx-background-color: linear-gradient(to right, #8a2be2, #9b30ff);" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 20;" +
                        "-fx-cursor: hand;" +
                        "-fx-padding: 8 20 8 20;" +
                        "-fx-font-size: 13px;"
        );

        newPasswordField.textProperty().addListener((obs, oldVal, newVal) -> {
            updateResetButton(resetButton, newVal, confirmPasswordField.getText());
        });
        confirmPasswordField.textProperty().addListener((obs, oldVal, newVal) -> {
            updateResetButton(resetButton, newPasswordField.getText(), newVal);
        });

        passwordDialog.setResultConverter(buttonType -> {
            if (buttonType == resetButtonType) {
                return new Pair<>(newPasswordField.getText(), confirmPasswordField.getText());
            }
            return null;
        });

        passwordDialog.showAndWait().ifPresent(passwords -> {
            if (passwords.getKey().equals(passwords.getValue())) {
                resetProgress.setVisible(true);

                Task<Void> resetTask = new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        userService.updatePasswordByEmail(email, passwords.getKey());
                        return null;
                    }
                };

                resetTask.setOnSucceeded(e -> {
                    resetProgress.setVisible(false);
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Password reset successfully!");
                    VerificationCodeManager.removeCode(email);
                });

                resetTask.setOnFailed(e -> {
                    resetProgress.setVisible(false);
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to reset password");
                });

                new Thread(resetTask).start();
            } else {
                showError("Passwords don't match");
            }
        });
    }


    private void showLoadingScreen(String message) {
        Platform.runLater(() -> {
            Stage loadingStage = new Stage();
            loadingStage.initStyle(StageStyle.UNDECORATED);

            VBox loadingBox = new VBox(20);
            loadingBox.setAlignment(Pos.CENTER);
            loadingBox.setStyle("-fx-background-color: rgba(255,255,255,0.9); -fx-padding: 30;");

            ProgressIndicator loadingSpinner = new ProgressIndicator();
            loadingSpinner.setStyle("-fx-progress-color: #3498db;");

            Label loadingLabel = new Label(message);
            loadingLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #333;");

            loadingBox.getChildren().addAll(loadingSpinner, loadingLabel);

            Scene loadingScene = new Scene(loadingBox, 300, 200);
            loadingStage.setScene(loadingScene);
            loadingStage.show();

            this.loadingStage = loadingStage;
        });
    }

    private void hideLoadingScreen() {
        Platform.runLater(() -> {
            if (loadingStage != null) {
                loadingStage.close();
                loadingStage = null;
            }
        });
    }

    private double calculatePasswordStrength(String password) {
        if (password == null || password.isEmpty()) return 0;

        double strength = 0;
        if (password.length() >= 8) strength += 0.3;
        if (password.matches(".*[A-Z].*")) strength += 0.2;
        if (password.matches(".*[a-z].*")) strength += 0.2;
        if (password.matches(".*\\d.*")) strength += 0.2;
        if (password.matches(".*[^A-Za-z0-9].*")) strength += 0.1;

        return Math.min(strength, 1.0);
    }

    private String getStrengthColor(double strength) {
        if (strength < 0.3) return "-fx-accent: #e74c3c;"; // Red
        if (strength < 0.6) return "-fx-accent: #f39c12;"; // Orange
        if (strength < 0.8) return "-fx-accent: #3498db;"; // Blue
        return "-fx-accent: #2ecc71;"; // Green
    }

    private void updateResetButton(Node button, String password, String confirmPassword) {
        boolean enabled = password.length() >= 6 && password.equals(confirmPassword);
        button.setDisable(!enabled);
    }

 }