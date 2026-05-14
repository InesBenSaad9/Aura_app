package org.example.ui.events;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.aura.entities.Event;
import org.example.services.EventService;

import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

@SuppressWarnings("unused")
public class EventFormController implements Initializable {

    @FXML private Label formTitle, errorLabel, imagePlaceholder;
    @FXML private TextField titleField, locationField, timeField, imageUrlField, priceField;
    @FXML private TextArea descField;
    @FXML private DatePicker datePicker;
    @FXML private Spinner<Integer> capacitySpinner, discountSpinner;
    @FXML private ComboBox<String> moodCombo, paymentCombo;
    @FXML private ImageView imagePreview;
    @FXML private StackPane imagePreviewPane;
    @FXML private Button saveBtn;

    private Event existing;
    private EventService service;
    private Runnable onRefresh;

    private static final String[] MOODS = {
            "neutre", "focus", "energy", "relax", "calme", "joyeux", "triste", "anxieux"
    };
    private static final String[] PAYMENT_METHODS = {
            "Gratuit", "Sur place", "Carte bancaire", "PayPal", "Virement", "Especes"
    };

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        moodCombo.getItems().addAll(MOODS);
        moodCombo.setValue("neutre");
        paymentCombo.getItems().addAll(PAYMENT_METHODS);
        paymentCombo.setValue("Gratuit");
        datePicker.setValue(LocalDate.now().plusDays(1));

        // Apercu image en temps reel
        imageUrlField.textProperty().addListener((obs, o, n) -> updateImagePreview(n));
    }

    public void setData(Event ev, EventService svc, Runnable refresh) {
        this.service = svc;
        this.onRefresh = refresh;

        if (ev != null) {
            this.existing = ev;
            formTitle.setText("Modifier l'evenement");
            saveBtn.setText("Enregistrer");

            titleField.setText(ev.getTitle() != null ? ev.getTitle() : "");
            descField.setText(ev.getDescription() != null ? ev.getDescription() : "");
            locationField.setText(ev.getLocation() != null ? ev.getLocation() : "");
            datePicker.setValue(ev.getEventDate() != null ? ev.getEventDate() : LocalDate.now().plusDays(1));
            timeField.setText(ev.getEventTime() != null ? ev.getEventTime() : "");
            if (capacitySpinner.getValueFactory() != null) {
                capacitySpinner.getValueFactory().setValue(ev.getMaxParticipants());
            }
            moodCombo.setValue(ev.getRecommendedMood() != null ? ev.getRecommendedMood() : "neutre");
            priceField.setText(formatPriceInput(ev.getPrice()));
            paymentCombo.setValue(ev.getPaymentMethod() != null ? ev.getPaymentMethod() : "Gratuit");
            if (discountSpinner.getValueFactory() != null) {
                discountSpinner.getValueFactory().setValue(ev.getDiscountPercent());
            }

            if (ev.getImageUrl() != null && !ev.getImageUrl().isBlank()) {
                imageUrlField.setText(ev.getImageUrl());
                updateImagePreview(ev.getImageUrl());
            }
        }
    }

    @FXML
    private void onBrowseImage() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choisir une image");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.webp")
        );
        File file = chooser.showOpenDialog(titleField.getScene().getWindow());
        if (file != null) {
            String uri = file.toURI().toString();
            imageUrlField.setText(uri);
            updateImagePreview(uri);
        }
    }

    private void updateImagePreview(String url) {
        if (url == null || url.isBlank()) {
            imagePreview.setImage(null);
            imagePlaceholder.setVisible(true);
            return;
        }
        try {
            Image img = new Image(url, 460, 90, true, true, true);
            img.errorProperty().addListener((obs, o, n) -> {
                if (n) {
                    imagePreview.setImage(null);
                    imagePlaceholder.setVisible(true);
                }
            });
            imagePreview.setImage(img);
            imagePlaceholder.setVisible(img.isError());
        } catch (Exception e) {
            imagePreview.setImage(null);
            imagePlaceholder.setVisible(true);
        }
    }

    @FXML
    private void onSave() {
        errorLabel.setText("");

        // Validations
        if (titleField.getText().trim().isEmpty()) {
            errorLabel.setText("Titre obligatoire.");
            return;
        }
        if (locationField.getText().trim().isEmpty()) {
            errorLabel.setText("Lieu obligatoire.");
            return;
        }
        if (datePicker.getValue() == null) {
            errorLabel.setText("Date obligatoire.");
            return;
        }
        if (existing == null && datePicker.getValue().isBefore(LocalDate.now())) {
            errorLabel.setText("La date doit etre dans le futur.");
            return;
        }
        if (timeField.getText().trim().isEmpty()) {
            errorLabel.setText("Heure obligatoire (HH:mm).");
            return;
        }
        if (!timeField.getText().trim().matches("^([01]?[0-9]|2[0-3]):[0-5][0-9]$")) {
            errorLabel.setText("Format heure invalide. Utilisez HH:mm (ex: 18:30).");
            return;
        }
        double price = parsePrice();
        if (price < 0) {
            errorLabel.setText("Prix invalide. Utilisez 0 pour un evenement gratuit.");
            return;
        }

        try {
            Event ev = existing != null ? existing : new Event();
            ev.setTitle(titleField.getText().trim());
            ev.setDescription(descField.getText().trim());
            ev.setLocation(locationField.getText().trim());
            ev.setEventDate(datePicker.getValue());
            ev.setEventTime(timeField.getText().trim());
            ev.setMaxParticipants(capacitySpinner.getValue());
            ev.setRecommendedMood(moodCombo.getValue());
            ev.setPrice(price);
            ev.setPaymentMethod(price > 0 ? paymentCombo.getValue() : "Gratuit");
            ev.setDiscountPercent(discountSpinner.getValue());
            String imgUrl = imageUrlField.getText().trim();
            ev.setImageUrl(imgUrl.isEmpty() ? null : imgUrl);
            ev.setCreatedBy(EventsViewController.CURRENT_USER_ID);

            if (existing != null) {
                service.updateEvent(ev);
            } else {
                service.createEvent(ev);
            }

            // Confirmation
            Alert ok = new Alert(Alert.AlertType.INFORMATION);
            ok.setTitle("Succes");
            ok.setHeaderText(null);
            Label successMessage = new Label(existing != null
                    ? "Evenement modifie avec succes."
                    : "Evenement cree avec succes.");
            successMessage.setStyle("-fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold;");
            successMessage.setWrapText(true);
            ok.getDialogPane().setContent(successMessage);
            ok.getDialogPane().setStyle("-fx-background-color: #12151A;");
            Button okBtn = (Button) ok.getDialogPane().lookupButton(ButtonType.OK);
            okBtn.setStyle("-fx-background-color: #1BBFA8; -fx-text-fill: white; -fx-background-radius: 6;");
            ok.showAndWait();

            if (onRefresh != null) onRefresh.run();
            close();

        } catch (Exception ex) {
            errorLabel.setText("Erreur : " + ex.getMessage());
        }
    }

    @FXML
    private void onCancel() {
        close();
    }

    private void close() {
        ((Stage) titleField.getScene().getWindow()).close();
    }

    private double parsePrice() {
        String raw = priceField.getText() == null ? "" : priceField.getText().trim().replace(",", ".");
        if (raw.isEmpty()) {
            return 0.0;
        }
        try {
            return Double.parseDouble(raw);
        } catch (NumberFormatException ex) {
            return -1.0;
        }
    }

    private String formatPriceInput(double price) {
        return price <= 0 ? "0" : String.format(java.util.Locale.US, "%.2f", price);
    }
}
