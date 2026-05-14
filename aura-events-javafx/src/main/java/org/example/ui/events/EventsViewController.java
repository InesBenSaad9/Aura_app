package org.example.ui.events;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.aura.entities.Event;
import org.example.services.EventService;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Alert;
import java.net.URL;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

@SuppressWarnings("unused")
public class EventsViewController implements Initializable {

    @FXML private FlowPane eventsFlow;
    @FXML private TextField searchField;
    @FXML private ToggleButton pillAll, pillFocus, pillEnergy, pillRelax, pillCalme, pillJoyeux;
    @FXML private HBox pillsBox;

    private EventService service;
    private String activeFilter = "all";
    private ToggleGroup pillGroup;

    public static final int CURRENT_USER_ID = 1;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        pillGroup = new ToggleGroup();
        pillAll.setToggleGroup(pillGroup);
        pillFocus.setToggleGroup(pillGroup);
        pillEnergy.setToggleGroup(pillGroup);
        pillRelax.setToggleGroup(pillGroup);
        pillCalme.setToggleGroup(pillGroup);
        pillJoyeux.setToggleGroup(pillGroup);

        searchField.textProperty().addListener((obs, o, n) -> loadEvents());
        loadEvents();
    }

    @FXML
    private void onFilter(javafx.event.ActionEvent e) {
        ToggleButton btn = (ToggleButton) e.getSource();
        activeFilter = (String) btn.getUserData();
        pillGroup.getToggles().forEach(t -> {
            ToggleButton tb = (ToggleButton) t;
            tb.setStyle("-fx-background-color: #1C2028; -fx-border-color: #252830;" +
                    "-fx-border-radius: 20; -fx-background-radius: 20;" +
                    "-fx-text-fill: #8A95A3; -fx-font-size: 12px;" +
                    "-fx-padding: 6 16 6 16; -fx-cursor: hand;");
        });
        btn.setStyle("-fx-background-color: #1BBFA822; -fx-border-color: #1BBFA8;" +
                "-fx-border-radius: 20; -fx-background-radius: 20;" +
                "-fx-text-fill: #1BBFA8; -fx-font-weight: bold;" +
                "-fx-font-size: 12px; -fx-padding: 6 16 6 16; -fx-cursor: hand;");
        loadEvents();
    }

    @FXML
    private void onCreateEvent() {
        openFormDialog(null);
    }

    public void loadEvents() {
        List<Event> events;
        try {
            EventService eventService = getService();
            events = eventService.getUpcomingEvents().stream()
                    .filter(this::matchesActiveFilter)
                    .sorted(Comparator
                            .comparing(Event::getEventDate, Comparator.nullsLast(Comparator.naturalOrder()))
                            .thenComparing(Event::getEventTime, Comparator.nullsLast(String::compareToIgnoreCase)))
                    .toList();
        } catch (Exception ex) {
            showError("Chargement impossible : " + rootMessage(ex));
            return;
        }

        String search = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        if (!search.isEmpty()) {
            events = events.stream()
                    .filter(ev -> ev.getTitle().toLowerCase().contains(search)
                            || (ev.getDescription() != null && ev.getDescription().toLowerCase().contains(search))
                            || (ev.getLocation() != null && ev.getLocation().toLowerCase().contains(search)))
                    .toList();
        }

        eventsFlow.getChildren().clear();
        if (events.isEmpty()) {
            eventsFlow.getChildren().add(buildEmptyState());
            return;
        }
        try {
            for (Event ev : events) {
                eventsFlow.getChildren().add(buildEventCard(ev));
            }
        } catch (Exception ex) {
            showError("Affichage impossible : " + rootMessage(ex));
        }
    }

    private VBox buildEventCard(Event ev) {
        VBox card = new VBox(0);
        card.setStyle("-fx-background-color: #12151A; -fx-background-radius: 14;" +
                "-fx-effect: dropshadow(gaussian, #00000066, 12, 0, 0, 4); -fx-cursor: hand;");
        card.setPrefWidth(300);

        // Image
        StackPane imgPane = new StackPane();
        imgPane.setPrefHeight(160);
        imgPane.setStyle("-fx-background-color: #1C2028; -fx-background-radius: 14 14 0 0;");

        if (ev.getImageUrl() != null && !ev.getImageUrl().isBlank()) {
            try {
                javafx.scene.image.Image img = new javafx.scene.image.Image(
                        ev.getImageUrl(), 300, 160, true, true, true);
                javafx.scene.image.ImageView iv = new javafx.scene.image.ImageView(img);
                iv.setFitWidth(300); iv.setFitHeight(160);
                iv.setPreserveRatio(false);
                imgPane.getChildren().add(iv);
            } catch (Exception ignored) {}
        }

        // Mood badge
        String mc = moodColor(ev.getRecommendedMood());
        Label moodBadge = new Label(moodLabel(ev.getRecommendedMood()));
        moodBadge.setStyle("-fx-background-color:" + mc + "cc; -fx-text-fill:white;" +
                "-fx-font-size:11px; -fx-font-weight:bold;" +
                "-fx-padding:4 10 4 10; -fx-background-radius:20;");
        StackPane.setAlignment(moodBadge, javafx.geometry.Pos.TOP_RIGHT);
        StackPane.setMargin(moodBadge, new javafx.geometry.Insets(10, 10, 0, 0));
        imgPane.getChildren().add(moodBadge);

        // Color bar
        HBox colorBar = new HBox();
        colorBar.setPrefHeight(3);
        colorBar.setStyle("-fx-background-color: " + mc + ";");

        // Content
        VBox content = new VBox(8);
        content.setPadding(new javafx.geometry.Insets(14));

        Label titleLbl = new Label(ev.getTitle());
        titleLbl.setStyle("-fx-font-size:15px; -fx-font-weight:bold; -fx-text-fill:#F0F2F5;");
        titleLbl.setWrapText(true);

        Label descLbl = new Label(ev.getDescription() != null ? ev.getDescription() : "");
        descLbl.setStyle("-fx-font-size:12px; -fx-text-fill:#8A95A3;");
        descLbl.setWrapText(true);
        descLbl.setMaxHeight(36);

        HBox dateMeta = new HBox(8);
        dateMeta.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Label dateLbl = new Label(ev.getEventDate() != null ? ev.getEventDate().toString() : "-");
        dateLbl.setStyle("-fx-font-size:12px; -fx-text-fill:#5A6070;");
        Label timeLbl = new Label(ev.getEventTime() != null ? ev.getEventTime() : "-");
        timeLbl.setStyle("-fx-font-size:12px; -fx-text-fill:#5A6070;");
        dateMeta.getChildren().addAll(dateLbl, timeLbl);

        HBox locMeta = new HBox(6);
        locMeta.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Label locLbl = new Label(ev.getLocation() != null ? ev.getLocation() : "-");
        locLbl.setStyle("-fx-font-size:12px; -fx-text-fill:#5A6070;");
        locMeta.getChildren().add(locLbl);

        Label priceLbl = new Label(priceBadgeText(ev));
        priceLbl.setStyle("-fx-background-color: #F59E0B22; -fx-border-color: #F59E0B66;"
                + "-fx-text-fill: #FBBF24; -fx-font-size: 11px; -fx-font-weight: bold;"
                + "-fx-padding: 3 10 3 10; -fx-background-radius: 20; -fx-border-radius: 20;");

        // Footer
        HBox footer = new HBox(8);
        footer.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        footer.setPadding(new javafx.geometry.Insets(10, 0, 0, 0));
        footer.setStyle("-fx-border-color: #252830; -fx-border-width: 1 0 0 0;");

        EventService.EventAvailability availability = getService().getEventAvailability(ev.getIdEvent());
        int count = availability.confirmedParticipants();
        int capacity = availability.capacity();
        ProgressBar progress = new ProgressBar(capacity == 0 ? 0 : (double) count / capacity);
        progress.setPrefWidth(90); progress.setPrefHeight(5);
        progress.setStyle("-fx-accent: " + mc + ";");

        Label spotsLbl = new Label(availability.available()
                ? count + "/" + capacity + " - " + availability.availableSpots() + " libres"
                : count + "/" + capacity + " - complet");
        spotsLbl.setStyle("-fx-font-size:11px; -fx-text-fill:" +
                (availability.available() ? "#5A6070" : "#E85D3A") + ";");

        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);

        // Bouton Modifier
        Button editBtn = new Button("Modifier");
        editBtn.setMinWidth(78);
        editBtn.setPrefWidth(78);
        editBtn.setStyle("-fx-background-color: #6B5FD422; -fx-border-color: #6B5FD4;" +
                "-fx-border-radius: 6; -fx-background-radius: 6;" +
                "-fx-text-fill: #6B5FD4; -fx-font-size:11px; -fx-font-weight:bold;" +
                "-fx-padding: 4 10 4 10; -fx-cursor:hand;");
        editBtn.setOnAction(e -> {
            e.consume();
            openFormDialog(ev);
        });

// Bouton Supprimer
        Button delBtn = new Button("Supprimer");
        delBtn.setMinWidth(86);
        delBtn.setPrefWidth(86);
        delBtn.setStyle("-fx-background-color: #E85D3A22; -fx-border-color: #E85D3A;" +
                "-fx-border-radius: 6; -fx-background-radius: 6;" +
                "-fx-text-fill: #E85D3A; -fx-font-size:11px; -fx-font-weight:bold;" +
                "-fx-padding: 4 10 4 10; -fx-cursor:hand;");
        delBtn.setOnAction(e -> {
            e.consume();
            // Popup confirmation
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Supprimer l'evenement");
            confirm.setHeaderText(null);
            VBox confirmContent = new VBox(8);
            Label confirmTitle = new Label("Supprimer \"" + ev.getTitle() + "\" ?");
            confirmTitle.setWrapText(true);
            confirmTitle.setStyle("-fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold;");
            Label confirmBody = new Label("Cette action est irreversible.");
            confirmBody.setWrapText(true);
            confirmBody.setStyle("-fx-text-fill: #D0D5DD; -fx-font-size: 13px;");
            confirmContent.getChildren().addAll(confirmTitle, confirmBody);
            confirm.getDialogPane().setContent(confirmContent);
            confirm.getDialogPane().setStyle("-fx-background-color: #12151A;");

            // Boutons styled
            ButtonType oui = new ButtonType("Supprimer", ButtonBar.ButtonData.OK_DONE);
            ButtonType non = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
            confirm.getButtonTypes().setAll(oui, non);

            Button ouiBtn = (Button) confirm.getDialogPane().lookupButton(oui);
            ouiBtn.setStyle("-fx-background-color: #E85D3A; -fx-text-fill: white;" +
                    "-fx-font-weight: bold; -fx-background-radius: 6;");
            Button nonBtn = (Button) confirm.getDialogPane().lookupButton(non);
            nonBtn.setStyle("-fx-background-color: #252830; -fx-text-fill: #D0D5DD;" +
                    "-fx-background-radius: 6;");

            confirm.showAndWait().ifPresent(b -> {
                if (b == oui) {
                    service.deleteEvent(ev.getIdEvent());
                    loadEvents();
                }
            });
        });

// Bouton Voir details
        Button detailBtn = new Button("Details");
        detailBtn.setMinWidth(72);
        detailBtn.setPrefWidth(72);
        detailBtn.setStyle("-fx-background-color: #1BBFA822; -fx-border-color: #1BBFA8;" +
                "-fx-border-radius: 6; -fx-background-radius: 6;" +
                "-fx-text-fill: #1BBFA8; -fx-font-size:11px; -fx-font-weight:bold;" +
                "-fx-padding: 4 10 4 10; -fx-cursor:hand;");
        detailBtn.setOnAction(e -> {
            e.consume();
            openDetailDialog(ev);
        });

        footer.getChildren().addAll(progress, spotsLbl, spacer, detailBtn, editBtn, delBtn);
        content.getChildren().addAll(titleLbl, descLbl, dateMeta, locMeta, priceLbl, footer);
        card.getChildren().addAll(imgPane, colorBar, content);

        card.setOnMouseClicked(e -> openDetailDialog(ev));
        card.setOnMouseEntered(e -> card.setStyle(
                "-fx-background-color: #1C2028; -fx-background-radius: 14;" +
                        "-fx-effect: dropshadow(gaussian, #1BBFA833, 18, 0, 0, 6); -fx-cursor: hand;"));
        card.setOnMouseExited(e -> card.setStyle(
                "-fx-background-color: #12151A; -fx-background-radius: 14;" +
                        "-fx-effect: dropshadow(gaussian, #00000066, 12, 0, 0, 4); -fx-cursor: hand;"));
        return card;
    }

    private void openFormDialog(Event existing) {
        try {
            URL fxmlUrl = getClass().getResource("/EventFormDialog.fxml");
            if (fxmlUrl == null) { System.err.println("FXML introuvable : EventFormDialog.fxml"); return; }
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
            EventFormController ctrl = loader.getController();
            ctrl.setData(existing, getService(), this::loadEvents);
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(existing == null ? "Creer un evenement" : "Modifier");
            stage.setScene(new Scene(root));
            stage.setMinWidth(540);
            stage.setHeight(720);
            stage.show();
        } catch (Exception ex) {
            showError("Erreur ouverture formulaire : " + rootMessage(ex));
        }
    }
    private void openDetailDialog(Event ev) {
        try {
            URL fxmlUrl = getClass().getResource("/EventDetailDialog.fxml");
            if (fxmlUrl == null) { System.err.println("EventDetailDialog.fxml introuvable"); return; }
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
            EventDetailController ctrl = loader.getController();
            ctrl.setData(ev, getService(), CURRENT_USER_ID, this::loadEvents);
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(ev.getTitle());
            stage.setScene(new Scene(root));
            stage.setMinWidth(620);
            stage.show();
        } catch (Exception ex) {
            showError("Erreur ouverture detail : " + rootMessage(ex));
        }
    }

    private VBox buildEmptyState() {
        VBox empty = new VBox(12);
        empty.setAlignment(javafx.geometry.Pos.CENTER);
        empty.setPadding(new javafx.geometry.Insets(80));
        Label msg = new Label("Aucun evenement pour l'instant");
        msg.setStyle("-fx-font-size:16px; -fx-text-fill:#8A95A3; -fx-font-weight:bold;");
        Label hint = new Label("Utilisez le bouton Nouvel evenement pour commencer.");
        hint.setStyle("-fx-font-size:13px; -fx-text-fill:#5A6070;");
        empty.getChildren().addAll(msg, hint);
        return empty;
    }

    private void showError(String msg) {
        eventsFlow.getChildren().clear();
        Label err = new Label("Erreur : " + msg);
        err.setStyle("-fx-text-fill:#E85D3A; -fx-font-size:13px; -fx-padding:40;");
        eventsFlow.getChildren().add(err);
    }

    private EventService getService() {
        if (service == null) {
            service = new EventService();
        }
        return service;
    }

    private int getCurrentParticipantCount(Event ev) {
        try {
            return getService().getCurrentParticipantCount(ev.getIdEvent());
        } catch (Exception ex) {
            return ev.getCurrentParticipantsCount();
        }
    }

    private static String rootMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        String message = current.getMessage();
        return message == null || message.isBlank() ? current.getClass().getSimpleName() : message;
    }

    private boolean matchesActiveFilter(Event event) {
        if ("all".equalsIgnoreCase(activeFilter)) {
            return true;
        }
        return normalizeMood(activeFilter).equals(normalizeMood(event.getRecommendedMood()));
    }

    private static String normalizeMood(String mood) {
        if (mood == null || mood.isBlank()) {
            return "neutre";
        }
        return mood.trim().toLowerCase(Locale.ROOT);
    }

    public static String moodColor(String mood) {
        if (mood == null) return "#5A6070";
        return switch (mood.toLowerCase()) {
            case "energy", "energise" -> "#E85D3A";
            case "focus"              -> "#6B5FD4";
            case "relax", "calme"     -> "#1BBFA8";
            case "joyeux", "happy"    -> "#E85D3A";
            case "triste", "sad"      -> "#6B5FD4";
            case "anxieux"            -> "#E85D3A";
            default                   -> "#5A6070";
        };
    }

    public static String moodLabel(String mood) {
        if (mood == null) return "Neutre";
        return switch (mood.toLowerCase()) {
            case "energy", "energise" -> "Energy";
            case "focus"              -> "Focus";
            case "relax"              -> "Relax";
            case "calme"              -> "Calme";
            case "joyeux"             -> "Happy";
            case "triste"             -> "Sad";
            case "anxieux"            -> "Anxieux";
            default -> mood.substring(0, 1).toUpperCase() + mood.substring(1);
        };
    }

    private String priceBadgeText(Event ev) {
        if (!ev.isPaidEvent()) {
            return "Gratuit";
        }
        if (ev.getDiscountPercent() > 0) {
            return String.format(java.util.Locale.US, "%.2f DT (-%d%%)",
                    ev.getDiscountedPrice(), ev.getDiscountPercent());
        }
        return String.format(java.util.Locale.US, "%.2f DT", ev.getPrice());
    }
}
