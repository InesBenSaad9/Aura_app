package org.example.ui.events;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import org.example.aura.entities.Event;
import org.example.services.EventService;

import java.awt.Desktop;
import java.net.URI;

public class UserEventDetailView extends BorderPane {

    private static final double DETAIL_MAX_WIDTH = 980;
    private static final double DETAIL_IMAGE_WIDTH = 560;
    private static final double DETAIL_IMAGE_HEIGHT = 220;

    private final Event event;
    private final EventService service;
    private final int currentUserId;
    private final Runnable onBack;

    public UserEventDetailView(Event event, EventService service, int currentUserId, Runnable onBack) {
        this.event = event;
        this.service = service;
        this.currentUserId = currentUserId;
        this.onBack = onBack;
        setStyle("-fx-background-color: #0A0C0F;");
        buildUI();
    }

    private void buildUI() {
        String mColor = EventsViewController.moodColor(event.getRecommendedMood());
        EventService.EventAvailability availability = service.getEventAvailability(event.getIdEvent());
        int count = availability.confirmedParticipants();
        int spots = availability.availableSpots();
        boolean joined = event.getParticipants().contains(currentUserId);
        boolean full = availability.full();

        //  SCROLLABLE CONTENT 
        VBox page = new VBox(0);
        page.setMaxWidth(DETAIL_MAX_WIDTH);
        page.setStyle("-fx-background-color: #0A0C0F;");

        // Hero banner
        VBox hero = new VBox(16);
        hero.setPadding(new Insets(40, 40, 32, 40));
        hero.setMaxWidth(DETAIL_MAX_WIDTH);
        hero.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #0A0C0F, #12151A);" +
                        "-fx-border-color: " + mColor + "44; -fx-border-width: 0 0 1 0;");

        // Back button
        Button backBtn = new Button("Retour aux evenements");
        backBtn.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: #8A95A3;" +
                        "-fx-font-size: 13px; -fx-padding: 0; -fx-cursor: hand;");
        backBtn.setOnMouseEntered(e -> backBtn.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: #F0F2F5;" +
                        "-fx-font-size: 13px; -fx-padding: 0; -fx-cursor: hand;"));
        backBtn.setOnMouseExited(e -> backBtn.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: #8A95A3;" +
                        "-fx-font-size: 13px; -fx-padding: 0; -fx-cursor: hand;"));
        backBtn.setOnAction(e -> goBack());

        // Mood badge + title
        Label moodBadge = new Label(EventsViewController.moodLabel(event.getRecommendedMood()).toUpperCase());
        moodBadge.setStyle(
                "-fx-background-color:" + mColor + "22; -fx-text-fill:" + mColor + ";" +
                        "-fx-border-color:" + mColor + "; -fx-font-size:11px; -fx-font-weight:bold;" +
                        "-fx-padding: 4 14 4 14; -fx-background-radius:20; -fx-border-radius:20;" +
                        "-fx-letter-spacing: 1px;");

        Label titleLbl = new Label(event.getTitle());
        titleLbl.setStyle(
                "-fx-font-size: 32px; -fx-font-weight: 900; -fx-text-fill: white;" +
                        "-fx-wrap-text: true;");
        titleLbl.setMaxWidth(680);

        // Color accent line under title
        Rectangle accentLine = new Rectangle(60, 4);
        accentLine.setStyle("-fx-fill: " + mColor + "; -fx-arc-width: 4; -fx-arc-height: 4;");

        StackPane eventImage = buildEventImage();
        hero.getChildren().addAll(backBtn, moodBadge, titleLbl, accentLine, eventImage);

        //  INFO SECTION 
        VBox infoSection = new VBox(28);
        infoSection.setPadding(new Insets(32, 40, 32, 40));
        infoSection.setMaxWidth(DETAIL_MAX_WIDTH);

        HBox infoGrid = new HBox(40);
        infoGrid.setAlignment(Pos.TOP_LEFT);

        // Left: description + details
        VBox leftCol = new VBox(24);
        HBox.setHgrow(leftCol, Priority.ALWAYS);

        // Description block
        VBox descBlock = new VBox(10);
        Label descTitle = new Label("A PROPOS DE L EVENEMENT");
        descTitle.setStyle("-fx-font-size: 11px; -fx-text-fill: " + mColor + "; -fx-font-weight: bold; -fx-letter-spacing: 1.5px;");
        Label descLbl = new Label(event.getDescription() != null && !event.getDescription().isBlank()
                ? event.getDescription() : "Aucune description fournie pour cet evenement.");
        descLbl.setStyle("-fx-font-size: 14px; -fx-text-fill: #D0D5DD; -fx-wrap-text: true; -fx-line-spacing: 4px;");
        descLbl.setMaxWidth(520);
        descBlock.getChildren().addAll(descTitle, descLbl);

        // Details grid
        VBox detailsBlock = new VBox(14);
        Label detailsTitle = new Label("INFORMATIONS");
        detailsTitle.setStyle("-fx-font-size: 11px; -fx-text-fill: " + mColor + "; -fx-font-weight: bold; -fx-letter-spacing: 1.5px;");
        VBox details = new VBox(12);
        details.getChildren().addAll(
                detailRow("Date", event.getEventDate() != null ? event.getEventDate().toString() : ""),
                detailRow("Heure", event.getEventTime() != null ? event.getEventTime() : ""),
                detailRow("Lieu", event.getLocation() != null ? event.getLocation() : ""),
                detailRow("Humeur ideale", EventsViewController.moodLabel(event.getRecommendedMood())),
                detailRow("Prix", formatPrice(event)),
                detailRow("Paiement", formatPayment(event))
        );
        detailsBlock.getChildren().addAll(detailsTitle, details);

        leftCol.getChildren().addAll(descBlock, detailsBlock);

        // Right: capacity card + action
        VBox rightCol = new VBox(16);
        rightCol.setMinWidth(220);
        rightCol.setMaxWidth(240);

        // Capacity card
        VBox capacityCard = new VBox(16);
        capacityCard.setPadding(new Insets(20));
        capacityCard.setStyle(
                "-fx-background-color: #1C2028; -fx-background-radius: 14;" +
                        "-fx-border-color: #2d3748; -fx-border-radius: 14;");

        Label capTitle = new Label("PARTICIPANTS");
        capTitle.setStyle("-fx-font-size: 10px; -fx-text-fill: #5A6070; -fx-font-weight: bold; -fx-letter-spacing: 1px;");

        HBox countRow = new HBox(6);
        countRow.setAlignment(Pos.CENTER_LEFT);
        Label countLbl = new Label(String.valueOf(count));
        countLbl.setStyle("-fx-font-size: 36px; -fx-font-weight: 900; -fx-text-fill: white;");
        Label ofLbl = new Label("/ " + availability.capacity());
        ofLbl.setStyle("-fx-font-size: 18px; -fx-text-fill: #5A6070; -fx-font-weight: bold; -fx-padding: 8 0 0 0;");
        countRow.getChildren().addAll(countLbl, ofLbl);

        // Progress bar
        double pct = availability.capacity() == 0 ? 0 : (double) count / availability.capacity();
        ProgressBar progress = new ProgressBar(pct);
        progress.setMaxWidth(Double.MAX_VALUE);
        progress.setStyle(
                "-fx-accent: " + (full ? "#E85D3A" : mColor) + ";" +
                        "-fx-background-color: #252830; -fx-background-radius: 4; -fx-pref-height: 6;");

        Label spotsLbl = new Label(full ? "Evenement complet" : spots + " place(s) disponible(s)");
        spotsLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: " + (full ? "#E85D3A" : "#1BBFA8") + ";");

        capacityCard.getChildren().addAll(capTitle, countRow, progress, spotsLbl);

        // JOIN / STATUS button (large)
        Button mainActionBtn;
        if (joined) {
            mainActionBtn = buildActionBtn("Vous etes inscrit",
                    "#1BBFA822", "#1BBFA8", "#1BBFA866", true);
            mainActionBtn.setOnAction(e -> showLeaveConfirm());
        } else if (full) {
            mainActionBtn = buildActionBtn("Evenement complet",
                    "#1C2028", "#5A6070", "#252830", true);
        } else {
            mainActionBtn = buildActionBtn("Participer a cet evenement",
                    "#1BBFA8", "white", "#1BBFA8", false);
            mainActionBtn.setOnAction(e -> showJoinPopup());
            // Pulse animation on hover
            mainActionBtn.setOnMouseEntered(ev2 -> mainActionBtn.setStyle(
                    "-fx-background-color: #059669; -fx-text-fill: white; -fx-background-radius: 12;" +
                            "-fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 14 0; -fx-cursor: hand;" +
                            "-fx-effect: dropshadow(gaussian, rgba(16,185,129,0.4), 20, 0, 0, 0);"));
            mainActionBtn.setOnMouseExited(ev2 -> mainActionBtn.setStyle(
                    "-fx-background-color: #1BBFA8; -fx-text-fill: white; -fx-background-radius: 12;" +
                            "-fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 14 0; -fx-cursor: hand;"));
        }

        Button mapsBtn = buildMapsButton();
        rightCol.getChildren().addAll(capacityCard, mapsBtn, mainActionBtn);
        infoGrid.getChildren().addAll(leftCol, rightCol);
        infoSection.getChildren().addAll(infoGrid, buildFeedbackSection());

        page.getChildren().addAll(hero, infoSection);

        StackPane pageWrapper = new StackPane(page);
        pageWrapper.setAlignment(Pos.TOP_CENTER);
        pageWrapper.setStyle("-fx-background-color: #0A0C0F;");

        ScrollPane scroll = new ScrollPane(pageWrapper);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background: #0A0C0F; -fx-background-color: #0A0C0F; -fx-border-color: transparent;");

        setCenter(scroll);
    }

    private Button buildActionBtn(String text, String bg, String fg, String border, boolean disabled) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setDisable(disabled && !bg.equals("#1BBFA822")); // allow "inscrit" btn for leave
        btn.setStyle(
                "-fx-background-color: " + bg + "; -fx-text-fill: " + fg + ";" +
                        "-fx-border-color: " + border + "; -fx-border-radius: 12; -fx-background-radius: 12;" +
                        "-fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 14 0; -fx-cursor: hand;");
        return btn;
    }

    private Button buildMapsButton() {
        Button mapsBtn = buildActionBtn("Voir sur Maps", "#2563EB22", "#60A5FA", "#2563EB66", false);
        boolean missingLocation = event.getLocation() == null || event.getLocation().isBlank();
        mapsBtn.setDisable(missingLocation);
        mapsBtn.setOnAction(e -> openExternalUrl(service.buildGoogleMapsUrl(event.getLocation())));
        return mapsBtn;
    }

    private VBox buildFeedbackSection() {
        VBox section = new VBox(12);
        section.setPadding(new Insets(18));
        section.setStyle("-fx-background-color: #12151A; -fx-background-radius: 12; -fx-border-color: #252830; -fx-border-radius: 12;");

        Label title = new Label("FEEDBACK");
        title.setStyle("-fx-font-size: 11px; -fx-text-fill: #1BBFA8; -fx-font-weight: bold; -fx-letter-spacing: 1.5px;");

        ComboBox<Integer> ratingBox = new ComboBox<>();
        ratingBox.getItems().addAll(1, 2, 3, 4, 5);
        ratingBox.setValue(5);
        ratingBox.setMaxWidth(120);
        ratingBox.setStyle("-fx-background-color: #1C2028; -fx-border-color: #252830; -fx-border-radius: 8; -fx-background-radius: 8; -fx-mark-color: white;");

        TextArea commentArea = new TextArea();
        commentArea.setPromptText("Votre avis sur cet evenement...");
        commentArea.setPrefRowCount(2);
        commentArea.setMaxHeight(90);
        commentArea.setWrapText(true);
        commentArea.setStyle("-fx-background-color: #1C2028; -fx-control-inner-background: #1C2028; -fx-text-fill: #F0F2F5; -fx-prompt-text-fill: #5A6070; -fx-border-color: #252830; -fx-border-radius: 8; -fx-background-radius: 8;");

        Button submitBtn = buildActionBtn("Envoyer feedback", "#1BBFA8", "white", "#1BBFA8", false);
        submitBtn.setMaxWidth(220);
        submitBtn.setOnAction(e -> {
            try {
                service.addFeedback(event.getIdEvent(), currentUserId, ratingBox.getValue(), commentArea.getText());
                showInfo("Merci, votre avis a ete enregistre avec succes.");
                commentArea.clear();
            } catch (RuntimeException ex) {
                showError("Feedback impossible : " + rootMessage(ex));
            }
        });

        HBox ratingRow = new HBox(10);
        ratingRow.setAlignment(Pos.CENTER_LEFT);
        Label ratingLabel = new Label("Note");
        ratingLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #D0D5DD;");
        ratingRow.getChildren().addAll(ratingLabel, ratingBox);

        section.getChildren().addAll(title, ratingRow, commentArea, submitBtn);
        return section;
    }

    private void openExternalUrl(String url) {
        try {
            if (url == null || url.isBlank()) {
                showError("Aucun lieu disponible pour cet evenement.");
                return;
            }
            if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                showError("Ouverture Maps indisponible sur cette machine.");
                return;
            }
            Desktop.getDesktop().browse(URI.create(url));
        } catch (Exception ex) {
            showError("Impossible d'ouvrir Maps : " + rootMessage(ex));
        }
    }

    private StackPane buildEventImage() {
        StackPane imagePane = new StackPane();
        imagePane.setMinSize(DETAIL_IMAGE_WIDTH, DETAIL_IMAGE_HEIGHT);
        imagePane.setPrefSize(DETAIL_IMAGE_WIDTH, DETAIL_IMAGE_HEIGHT);
        imagePane.setMaxSize(DETAIL_IMAGE_WIDTH, DETAIL_IMAGE_HEIGHT);
        imagePane.setStyle(
                "-fx-background-color: #1C2028;" +
                        "-fx-background-radius: 14;" +
                        "-fx-border-color: #252830; -fx-border-radius: 14;");

        if (event.getImageUrl() != null && !event.getImageUrl().isBlank()) {
            ImageView imageView = new ImageView(new Image(event.getImageUrl(), DETAIL_IMAGE_WIDTH, DETAIL_IMAGE_HEIGHT, true, true, true));
            imageView.setFitWidth(DETAIL_IMAGE_WIDTH);
            imageView.setFitHeight(DETAIL_IMAGE_HEIGHT);
            imageView.setPreserveRatio(true);
            imageView.setSmooth(true);
            imagePane.getChildren().add(imageView);
            return imagePane;
        }

        Label placeholder = new Label("No image");
        placeholder.setStyle("-fx-font-size: 16px; -fx-text-fill: #5A6070;");
        imagePane.getChildren().add(placeholder);
        return imagePane;
    }

    private HBox detailRow(String label, String value) {
        HBox row = new HBox(14);
        row.setAlignment(Pos.CENTER_LEFT);
        VBox txt = new VBox(2);
        Label lbl = new Label(label.toUpperCase());
        lbl.setStyle("-fx-font-size: 10px; -fx-text-fill: #5A6070; -fx-font-weight: bold;");
        Label val = new Label(value);
        val.setStyle("-fx-font-size: 13px; -fx-text-fill: #F0F2F5;");
        txt.getChildren().addAll(lbl, val);
        row.getChildren().add(txt);
        return row;
    }

    private String formatPrice(Event event) {
        if (!event.isPaidEvent()) {
            return "Gratuit";
        }
        if (event.getDiscountPercent() > 0) {
            return String.format(java.util.Locale.US, "%.2f DT -> %.2f DT",
                    event.getPrice(), event.getDiscountedPrice());
        }
        return String.format(java.util.Locale.US, "%.2f DT", event.getPrice());
    }

    private String formatPayment(Event event) {
        if (!event.isPaidEvent()) {
            return "Aucun paiement";
        }
        String method = event.getPaymentMethod() == null || event.getPaymentMethod().isBlank()
                ? "Sur place"
                : event.getPaymentMethod();
        return event.getDiscountPercent() > 0
                ? method + " - Reduction " + event.getDiscountPercent() + "%"
                : method;
    }

    //  JOIN POPUP 
    private void showJoinPopup() {
        JoinConfirmDialog popup = new JoinConfirmDialog(event, service, currentUserId, () -> {
            // Refresh: reload this detail view with fresh data
            service.getEventById(event.getIdEvent()).ifPresent(fresh -> {
                StackPane contentArea = (StackPane) getScene().getRoot().lookup(".content-area");
                if (contentArea != null) {
                    contentArea.getChildren().setAll(
                            new UserEventDetailView(fresh, service, currentUserId, onBack));
                }
            });
        });
        popup.showAndWait();
    }

    //  LEAVE CONFIRM 
    private void showLeaveConfirm() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Quitter l evenement");
        alert.setHeaderText(null);
        alert.getDialogPane().setContent(alertMessage(
                "Voulez-vous vraiment quitter \"" + event.getTitle() + "\" ?"
        ));
        alert.getDialogPane().setStyle("-fx-background-color: #1C2028; -fx-font-size: 13px;");
        alert.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                service.leaveEvent(event.getIdEvent(), currentUserId);
                service.getEventById(event.getIdEvent()).ifPresent(fresh -> {
                    StackPane contentArea = (StackPane) getScene().getRoot().lookup(".content-area");
                    if (contentArea != null) {
                        contentArea.getChildren().setAll(
                                new UserEventDetailView(fresh, service, currentUserId, onBack));
                    }
                });
            }
        });
    }

    private void goBack() {
        StackPane contentArea = (StackPane) getScene().getRoot().lookup(".content-area");
        if (contentArea != null) {
            UserEventsView listView = new UserEventsView();
            contentArea.getChildren().setAll(listView);
            if (onBack != null) onBack.run();
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, "", ButtonType.OK);
        alert.setHeaderText(null);
        alert.getDialogPane().setStyle("-fx-background-color: #1C2028;");
        alert.getDialogPane().setContent(alertMessage(message));
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "", ButtonType.OK);
        alert.setHeaderText(null);
        alert.getDialogPane().setStyle("-fx-background-color: #1C2028;");
        alert.getDialogPane().setContent(alertMessage(message));
        alert.showAndWait();
    }

    private Label alertMessage(String message) {
        Label label = new Label(message);
        label.setWrapText(true);
        label.setStyle("-fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold;");
        return label;
    }

    private static String rootMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        String message = current.getMessage();
        return message == null || message.isBlank() ? current.getClass().getSimpleName() : message;
    }
}
