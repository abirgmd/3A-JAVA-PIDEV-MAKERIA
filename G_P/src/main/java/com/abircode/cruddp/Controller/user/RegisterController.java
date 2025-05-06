package com.abircode.cruddp.Controller.user;

import com.abircode.cruddp.entities.User;
import com.abircode.cruddp.services.user.TranslationService;
import com.abircode.cruddp.services.user.UserService;
import com.abircode.cruddp.utils.FileUploader;
import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.regex.Pattern;

public class RegisterController {
    // FXML Components
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField nameField;
    @FXML private TextField lastnameField;
    @FXML private TextField phoneField;
    @FXML private Label errorLabel;
    @FXML private ImageView profileImageView;
    @FXML private ProgressBar progressBar;
    @FXML private Label progressLabel;
    @FXML private Button registerButton;


    @FXML
    private Label password;


    @FXML
    private Label labelEmail;

    @FXML
    private Label labelFirstName;

    @FXML
    private Label labelLastName;

    @FXML
    private Label phoneLabel;

    private File selectedImageFile;
    private final UserService userService = new UserService();
    private Webcam webcam;
    private boolean isProcessing = false;

    // Constants
    private static final int MIN_NAME_LENGTH = 2;
    private static final int MIN_PASSWORD_LENGTH = 8;
    private final TranslationService translationService =
            TranslationService.getInstance("b2c58875-8344-4581-bdf6-2204fa8ec37e:fx");

    private static final String EMAIL_REGEX =
            "^(?=.{1,64}@)[\\p{L}0-9_-]+(\\.[\\p{L}0-9_-]+)*@"
                    + "[^-][\\p{L}0-9-]+(\\.[\\p{L}0-9-]+)*(\\.[\\p{L}]{2,})$";
    private static final String PHONE_REGEX = "^\\+216\\d{8}$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);
    private static final Pattern PHONE_PATTERN = Pattern.compile(PHONE_REGEX);

    @FXML
    public void initialize() {
        // In your initialize() method:
        languageCombo.getItems().addAll("English", "French", "German", "Spanish" , "Arabe");
        languageCombo.getSelectionModel().select(0); // Default to English
        translateUI();
        setupProgressTracking();
        setupFieldValidators();
        loadDefaultAvatar();
    }
    @FXML
    private ComboBox<String> languageCombo;

    @FXML
    private void handleLanguageChange() {
        String selected = languageCombo.getSelectionModel().getSelectedItem();
        Locale locale;

        switch (selected) {
            case "French":
                locale = Locale.FRENCH;
                break;
            case "Arabe":
                locale = new Locale("ar");
                break;
            case "German":
                locale = Locale.GERMAN;
                break;
            case "Spanish":
                locale = new Locale("es");
                break;
            default:
                locale = Locale.ENGLISH;
        }

        translationService.setLocale(locale);
        translateUI(); // Refresh all UI text
    }
    private boolean isContentAppropriate(String content) {
        try {
            URL url = new URL("https://www.purgomalum.com/service/containsprofanity?text=" +
                    URLEncoder.encode(content, StandardCharsets.UTF_8));
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000); // Timeout de 5 secondes
            conn.setReadTimeout(5000);

            String response = new String(conn.getInputStream().readAllBytes());
            return Boolean.parseBoolean(response);
        } catch (Exception e) {
            System.err.println("Erreur lors de la vérification du contenu: " + e.getMessage());
            return false; // En cas d'erreur, on considère que le contenu est approprié
        }
    }
    private boolean containsInappropriateLanguage(String content, String fieldName) {
        if (isContentAppropriate(content)) {
            errorLabel.setText("error  Le champ " + fieldName + " contient un langage inapproprié. Veuillez le modifier.");
            return true;
        }
        return false;
    }

    private void translateUI() {
        // Translate labels
        labelEmail.setText(translationService.getTranslation("Email"));
        password.setText(translationService.getTranslation("Password"));
        labelFirstName.setText(translationService.getTranslation("First Name"));
        labelLastName.setText(translationService.getTranslation("Last Name"));
        phoneLabel.setText(translationService.getTranslation("Phone"));

        // Translate buttons
        registerButton.setText(translationService.getTranslation("Register"));

        // Translate field prompts
        emailField.setPromptText(translationService.getTranslation("Enter email"));
        passwordField.setPromptText(translationService.getTranslation("Create password"));
        nameField.setPromptText(translationService.getTranslation("Your first name"));
        lastnameField.setPromptText(translationService.getTranslation("Your last name"));
        phoneField.setPromptText(translationService.getTranslation("Optional"));

        // Translate other UI elements
        progressLabel.setText(translationService.getTranslation("0% Complete"));

    }

    private void setupProgressTracking() {
        progressBar.setProgress(0);
        progressLabel.setText("0% Complete");

        emailField.textProperty().addListener((obs, oldVal, newVal) -> updateRegistrationProgress());
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> updateRegistrationProgress());
        nameField.textProperty().addListener((obs, oldVal, newVal) -> updateRegistrationProgress());
        lastnameField.textProperty().addListener((obs, oldVal, newVal) -> updateRegistrationProgress());
    }
    private void setupFieldValidators() {
        // Real-time validation for email
        emailField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) validateEmail();
        });

        // Real-time validation for password
        passwordField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) validatePassword();
        });

        // Real-time validation for name fields
        nameField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) validateNameField(nameField, "First name");
        });

        lastnameField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) validateNameField(lastnameField, "Last name");
        });

        // Phone number formatting
        phoneField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("[+\\d]*")) {
                phoneField.setText(oldVal);
            }
        });
    }

    private void loadDefaultAvatar() {
        try {
            Image defaultImage = new Image(getClass().getResourceAsStream("/images/default-avatar.png"));
            profileImageView.setImage(defaultImage);
        } catch (Exception e) {
            System.err.println("Error loading default avatar: " + e.getMessage());
        }
    }

    private void updateRegistrationProgress() {
        if (isProcessing) return;

        double progress = 0;
        int totalFields = 4; // email, password, first name, last name
        int completedFields = 0;

        if (isValidEmail(emailField.getText())) completedFields++;
        if (isValidPassword(passwordField.getText())) completedFields++;
        if (isValidName(nameField.getText())) completedFields++;
        if (isValidName(lastnameField.getText())) completedFields++;

        progress = (double) completedFields / totalFields;
        progressBar.setProgress(progress);
        progressLabel.setText((int)(progress * 100) + "% Complete");
    }
    private boolean isValidName(String name) {
        return !name.isEmpty() && name.length() >= MIN_NAME_LENGTH;
    }
    @FXML
    private void handleRegister() {
        if (isProcessing) return;

        if (!validateAllFields()) {
            return;
        }

        isProcessing = true;
        registerButton.setDisable(true);
        progressBar.setProgress(0);
        progressLabel.setText("Processing registration...");
        clearErrorStyles();

        Task<Void> registerTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                updateProgress(0.1, 1.0);

                // Get validated fields
                String email = emailField.getText().trim();
                String password = passwordField.getText().trim();
                String name = nameField.getText().trim();
                String lastname = lastnameField.getText().trim();
                String phoneText = phoneField.getText().trim();

                // Create user
                User newUser = new User();
                newUser.setEmail(email);
                newUser.setPassword(password);
                newUser.setName(name);
                newUser.setLastname(lastname);
                newUser.setBlok(false);
                newUser.setAccepted(null);

                // Handle phone number
                if (!phoneText.isEmpty()) {
                    String phoneNumber = phoneText.replace("+216", "");
                    newUser.setPhone(Integer.parseInt(phoneNumber));
                }

                updateProgress(0.3, 1.0);

                // Handle profile image
                if (selectedImageFile != null) {
                    String uploadedFileName = FileUploader.uploadImage(selectedImageFile);
                    newUser.setImage(uploadedFileName);
                } else {
                    newUser.setImage("default.png");
                }

                updateProgress(0.5, 1.0);

                // Save user
                try {
                    userService.createUser(newUser);
                    updateProgress(1.0, 1.0);
                    return null;
                } catch (Exception e) {
                    Platform.runLater(() ->
                            errorLabel.setText("Registration failed: " + e.getMessage()));
                    throw e;
                }
            }
        };

        registerTask.setOnSucceeded(e -> {
            isProcessing = false;
            registerButton.setDisable(false);
            progressBar.setProgress(1);
            progressLabel.setText("Registration complete!");
            errorLabel.setStyle("-fx-text-fill: #4caf50;");
            errorLabel.setText("Registration successful!");
            clearForm();
            showSuccessAlert();
        });

        registerTask.setOnFailed(e -> {
            isProcessing = false;
            registerButton.setDisable(false);
            progressBar.setProgress(0);
            progressLabel.setText("Registration failed");
            errorLabel.setStyle("-fx-text-fill: #ff5252;");

            if (registerTask.getException() != null) {
                errorLabel.setText("Registration failed: " +
                        registerTask.getException().getMessage());
            } else {
                errorLabel.setText("Registration failed. Please try again.");
            }
        });

        new Thread(registerTask).start();
    }
    private boolean validateAllFields() {
        boolean allValid = true;
        clearErrorStyles(); // Clear all errors before re-validating

        // Validate each field and track overall validity
        if (!validateNameField(nameField, "First name") || containsInappropriateLanguage(nameField.getText(), "First name")) allValid = false;
        if (!validateNameField(lastnameField, "Last name") || containsInappropriateLanguage(lastnameField.getText(), "Last name")) allValid = false;
        if (!validateEmail() || containsInappropriateLanguage(emailField.getText(), "Email")) allValid = false;
        if (!validatePassword()) allValid = false;
        if (!validatePhone() || (!phoneField.getText().isEmpty() && containsInappropriateLanguage(phoneField.getText(), "Phone"))) allValid = false;


        // Only show error label if there are actual errors
        if (allValid) {
            errorLabel.setText("");
        }

        return allValid;
    }
    private boolean validateNameField(TextField field, String fieldName) {
        String text = field.getText().trim();
        if (text.isEmpty()) {
            showFieldError(field, fieldName + " is required");
            return false;
        } else if (text.length() < MIN_NAME_LENGTH) {
            showFieldError(field, fieldName + " must be at least " + MIN_NAME_LENGTH + " characters");
            return false;
        }
        clearFieldError(field);
        return true;
    }
    private boolean validateEmail() {
        String email = emailField.getText().trim();
        if (email.isEmpty()) {
            showFieldError(emailField, "Email is required");
            return false;
        } else if (!EMAIL_PATTERN.matcher(email).matches()) {
            showFieldError(emailField, "Invalid email format");
            return false;
        }
        clearFieldError(emailField);
        return true;
    }
    private boolean isValidEmail(String email) {
        return !email.isEmpty() && EMAIL_PATTERN.matcher(email).matches();
    }

    private boolean validatePassword() {
        String password = passwordField.getText().trim();
        if (password.isEmpty()) {
            showFieldError(passwordField, "Password is required");
            return false;
        } else if (password.length() < MIN_PASSWORD_LENGTH) {
            showFieldError(passwordField, "Password must be at least " + MIN_PASSWORD_LENGTH + " characters");
            return false;
        }
        clearFieldError(passwordField);
        return true;
    }    private boolean isValidPassword(String password) {
        return !password.isEmpty() && password.length() >= 8;
    }

    private boolean validatePhone() {
        String phone = phoneField.getText().trim();
        if (!phone.isEmpty() && !PHONE_PATTERN.matcher(phone).matches()) {
            if (phone.matches("\\d{8}")) {
                Platform.runLater(() -> phoneField.setText("+216" + phone));
                clearFieldError(phoneField);
                return true;
            }
            showFieldError(phoneField, "Phone must be in +216XXXXXXXX format");
            return false;
        }
        clearFieldError(phoneField);
        return true;
    }
    private void showFieldError(TextField field, String message) {
        field.setStyle("-fx-border-color: #ff5252; -fx-border-width: 1px;");
        errorLabel.setText(message);
    }

    private void clearFieldError(TextField field) {
        field.setStyle("");
    }


    private void clearErrorStyles() {
        emailField.setStyle("");
        passwordField.setStyle("");
        nameField.setStyle("");
        lastnameField.setStyle("");
        phoneField.setStyle("");
        errorLabel.setText(""); // Clear the error message
    }

    @FXML
    private void handlePhoneNumberInput(KeyEvent event) {
        TextField field = (TextField) event.getSource();
        String text = field.getText();
        String character = event.getCharacter();

        // Allow only digits and + at start
        if (!character.matches("[\\d+]") ||
                (text.contains("+") && character.equals("+"))) {
            event.consume();
            return;
        }

        // Auto-format to +216 when user types 8 digits
        if (text.length() == 8 && text.matches("\\d{8}")) {
            Platform.runLater(() -> field.setText("+216" + text));
        }

        // Limit length to 12 characters (+216 + 8 digits)
        if (text.length() >= 12 && !event.isShortcutDown()) {
            event.consume();
        }
    }

    @FXML
    private void handleImageUpload() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        File file = fileChooser.showOpenDialog(profileImageView.getScene().getWindow());
        if (file != null) {
            selectedImageFile = file;
            profileImageView.setImage(new Image(file.toURI().toString()));
        }
    }

    @FXML
    private void handleTakePhoto(ActionEvent event) {
        Stage cameraStage = new Stage();
        cameraStage.initModality(Modality.APPLICATION_MODAL);
        cameraStage.setTitle("Take Photo");
        cameraStage.initStyle(StageStyle.UTILITY);

        VBox cameraBox = new VBox(10);
        cameraBox.setStyle("-fx-background-color: #2d2d2d; -fx-padding: 20;");

        webcam = Webcam.getDefault();
        if (webcam == null) {
            showAlert("Error", "No webcam detected");
            return;
        }

        webcam.setViewSize(WebcamResolution.VGA.getSize());
        ImageView previewImage = new ImageView();
        previewImage.setFitWidth(640);
        previewImage.setFitHeight(480);
        previewImage.setPreserveRatio(true);

        // Webcam capture task
        Task<Void> webcamTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                while (!isCancelled()) {
                    BufferedImage image = webcam.getImage();
                    if (image != null) {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        ImageIO.write(image, "jpg", baos);
                        Image fxImage = new Image(new ByteArrayInputStream(baos.toByteArray()));
                        Platform.runLater(() -> previewImage.setImage(fxImage));
                    }
                    Thread.sleep(50);
                }
                return null;
            }
        };

        Button captureBtn = new Button("Capture Photo");
        captureBtn.setStyle("-fx-background-color: #9c27b0; -fx-text-fill: white;");
        captureBtn.setOnAction(e -> {
            BufferedImage capturedImage = webcam.getImage();
            try {
                selectedImageFile = File.createTempFile("webcam", ".jpg");
                ImageIO.write(capturedImage, "jpg", selectedImageFile);
                profileImageView.setImage(new Image(selectedImageFile.toURI().toString()));
                webcamTask.cancel();
                webcam.close();
                cameraStage.close();
            } catch (IOException ex) {
                Platform.runLater(() ->
                        errorLabel.setText("Error saving photo: " + ex.getMessage()));
            }
        });

        cameraBox.getChildren().addAll(previewImage, captureBtn);
        Scene scene = new Scene(cameraBox);
        cameraStage.setScene(scene);
        cameraStage.setOnCloseRequest(e -> {
            webcamTask.cancel();
            if (webcam != null) webcam.close();
        });

        new Thread(webcamTask).start();
        cameraStage.showAndWait();
    }

    private void clearForm() {
        emailField.clear();
        passwordField.clear();
        nameField.clear();
        lastnameField.clear();
        phoneField.clear();
        loadDefaultAvatar();
        selectedImageFile = null;
        progressBar.setProgress(0);
        progressLabel.setText("0% Complete");
    }

    @FXML
    private void goToLogin(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setFullScreen(true);

            stage.setTitle("Login");
            stage.show();
        } catch (IOException e) {
            showAlert("Navigation Error", "Failed to load login view: " + e.getMessage());
        }
    }

    private void showSuccessAlert() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Registration Successful");
        alert.setHeaderText(null);
        alert.setContentText("Your account has been created successfully!");
        alert.showAndWait();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}