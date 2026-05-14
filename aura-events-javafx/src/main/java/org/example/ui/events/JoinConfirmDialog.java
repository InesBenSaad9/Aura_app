package org.example.ui.events;

import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import org.example.aura.entities.Event;
import org.example.services.EventService;

import java.awt.Desktop;
import java.net.URI;

public class JoinConfirmDialog extends Dialog<Boolean> {

    private final Event event;
    private final EventService service;
    private final int currentUserId;
    private final Runnable onSuccess;

    public JoinConfirmDialog(Event event, EventService service, int currentUserId, Runnable onSuccess) {
        this.event = event;
        this.service = service;
        this.currentUserId = currentUserId;
        this.onSuccess = onSuccess;

        setTitle("Inscription a l evenement");
        getDialogPane().setStyle(
                "-fx-background-color: #0A0C0F; -fx-background-radius: 16;");
        getDialogPane().setPrefWidth(440);
        getDialogPane().setPrefHeight(420);

        getDialogPane().setContent(buildContent());

        // No default buttons  we handle them manually inside
        getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        Button cancelBtn = (Button) getDialogPane().lookupButton(ButtonType.CANCEL);
        cancelBtn.setVisible(false); // hidden  we have our own cancel inside
    }

    private VBox buildContent() {
        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: #0A0C0F;");

        String mColor = EventsViewController.moodColor(event.getRecommendedMood());
        EventService.EventAvailability availability = service.getEventAvailability(event.getIdEvent());
        int spots = availability.availableSpots();

        //  TOP: colored gradient header 
        VBox topBanner = new VBox(12);
        topBanner.setAlignment(Pos.CENTER);
        topBanner.setPadding(new Insets(28, 24, 24, 24));
        topBanner.setStyle(
                "-fx-background-color: linear-gradient(to bottom, " + mColor + "22, transparent);");

        // Big icon circle
        StackPane iconCircle = new StackPane();
        Circle circleBg = new Circle(36);
        circleBg.setStyle("-fx-fill: " + mColor + "33; -fx-stroke: " + mColor + "; -fx-stroke-width: 2;");
        iconCircle.getChildren().add(circleBg);

        Label confirmTitle = new Label("Confirmer l'inscription");
        confirmTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: 900; -fx-text-fill: white;");

        Label confirmSub = new Label("Vous etes sur le point de rejoindre cet evenement");
        confirmSub.setStyle("-fx-font-size: 12px; -fx-text-fill: #8A95A3; -fx-wrap-text: true;");
        confirmSub.setMaxWidth(340);

        topBanner.getChildren().addAll(iconCircle, confirmTitle, confirmSub);

        //  EVENT SUMMARY CARD 
        VBox summaryCard = new VBox(12);
        summaryCard.setPadding(new Insets(18));
        summaryCard.setMargin(summaryCard, new Insets(0, 24, 0, 24));
        summaryCard.setStyle(
                "-fx-background-color: #1C2028; -fx-background-radius: 12;" +
                        "-fx-border-color: " + mColor + "44; -fx-border-radius: 12; -fx-border-width: 1;");

        HBox titleRow = new HBox(10);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        VBox leftAccent = new VBox();
        leftAccent.setMinWidth(3); leftAccent.setMinHeight(40);
        leftAccent.setStyle("-fx-background-color: " + mColor + "; -fx-background-radius: 2;");
        Label evTitle = new Label(event.getTitle());
        evTitle.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: white; -fx-wrap-text: true;");
        evTitle.setMaxWidth(340);
        titleRow.getChildren().addAll(leftAccent, evTitle);

        // Info chips
        HBox chips = new HBox(16);
        chips.setAlignment(Pos.CENTER_LEFT);
        chips.setPadding(new Insets(0, 0, 0, 12));
        chips.getChildren().addAll(
                infoChip(event.getEventDate() != null ? event.getEventDate().toString() : ""),
                infoChip( event.getEventTime() != null ? event.getEventTime() : ""),
                infoChip( event.getLocation() != null ? event.getLocation() : "")
        );

        // Spots info
        HBox spotsRow = new HBox(8);
        spotsRow.setAlignment(Pos.CENTER_LEFT);
        spotsRow.setPadding(new Insets(0, 0, 0, 12));
        Label spotsLbl = new Label(availability.available()
                ? spots + " place(s) disponible(s) sur " + availability.capacity()
                : "Places insuffisantes : inscription non acceptee");
        spotsLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: " +
                (availability.available() ? "#1BBFA8" : "#E85D3A") + ";");
        spotsRow.getChildren().add(spotsLbl);

        summaryCard.getChildren().addAll(titleRow, chips, spotsRow);

        //  BUTTONS 
        VBox buttonsSection = new VBox(10);
        buttonsSection.setPadding(new Insets(20, 24, 24, 24));

        Button confirmBtn = new Button(availability.available()
                ? "Confirmer l inscription"
                : "Inscription indisponible");
        confirmBtn.setDisable(!availability.available());
        confirmBtn.setMaxWidth(Double.MAX_VALUE);
        confirmBtn.setStyle(
                "-fx-background-color: " + mColor + "; -fx-text-fill: white;" +
                        "-fx-background-radius: 10; -fx-font-size: 14px; -fx-font-weight: bold;" +
                        "-fx-padding: 13 0; -fx-cursor: hand;");
        confirmBtn.setOnMouseEntered(e -> confirmBtn.setStyle(
                "-fx-background-color: #059669; -fx-text-fill: white;" +
                        "-fx-background-radius: 10; -fx-font-size: 14px; -fx-font-weight: bold;" +
                        "-fx-padding: 13 0; -fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(16,185,129,0.35), 16, 0, 0, 0);"));
        confirmBtn.setOnMouseExited(e -> confirmBtn.setStyle(
                "-fx-background-color: " + mColor + "; -fx-text-fill: white;" +
                        "-fx-background-radius: 10; -fx-font-size: 14px; -fx-font-weight: bold;" +
                        "-fx-padding: 13 0; -fx-cursor: hand;"));
        confirmBtn.setOnAction(e -> handleJoin());

        Button cancelBtn2 = new Button("Annuler");
        cancelBtn2.setMaxWidth(Double.MAX_VALUE);
        cancelBtn2.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: #8A95A3;" +
                        "-fx-border-color: #252830; -fx-border-radius: 10; -fx-background-radius: 10;" +
                        "-fx-font-size: 13px; -fx-padding: 11 0; -fx-cursor: hand;");
        cancelBtn2.setOnAction(e -> close());

        buttonsSection.getChildren().addAll(confirmBtn, cancelBtn2);

        // Padding wrapper for summary card
        VBox summaryWrapper = new VBox();
        summaryWrapper.setPadding(new Insets(0, 24, 0, 24));
        summaryWrapper.getChildren().add(summaryCard);

        root.getChildren().addAll(topBanner, summaryWrapper, buttonsSection);
        return root;
    }

    private HBox infoChip(String text) {
        HBox chip = new HBox(5);
        chip.setAlignment(Pos.CENTER_LEFT);
        Label t = new Label(text);
        t.setStyle("-fx-font-size: 11px; -fx-text-fill: #8A95A3;");
        chip.getChildren().add(t);
        return chip;
    }

    private void handleJoin() {
        try {
            EventService.ParticipationDecision decision =
                    service.requestParticipation(event.getIdEvent(), currentUserId);
            if (decision.accepted()) {
                showSuccessState(decision.message());
            } else {
                showRefusedState(decision.message(), decision.availability());
            }
        } catch (RuntimeException ex) {
            showError("Inscription impossible : " + rootMessage(ex));
        }
    }

    private void showSuccessState(String message) {
        EventService.ParticipationTicket ticket = service.createParticipationTicket(event.getIdEvent(), currentUserId);
        getDialogPane().setContent(buildAcceptedContent(
                "Inscription confirmee",
                message,
                "#1BBFA8",
                ticket
        ));
    }

    private void showRefusedState(String message, EventService.EventAvailability availability) {
        String detail = message;
        if (availability != null && availability.capacity() > 0) {
            detail += " (" + availability.confirmedParticipants() + "/" + availability.capacity() + " participants)";
        }
        getDialogPane().setContent(buildDecisionContent(
                "Inscription refusee",
                detail,
                "#E85D3A",
                "Fermer",
                false
        ));
    }

    private VBox buildDecisionContent(String title, String message, String color, String buttonText, boolean accepted) {
        VBox content = new VBox(20);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(40, 30, 30, 30));
        content.setStyle("-fx-background-color: #0A0C0F;");
        content.setPrefHeight(340);

        StackPane checkCircle = new StackPane();
        Circle bg = new Circle(44);
        bg.setStyle("-fx-fill: " + color + "22; -fx-stroke: " + color + "; -fx-stroke-width: 2.5;");
        checkCircle.getChildren().add(bg);

        ScaleTransition scale = new ScaleTransition(Duration.millis(400), checkCircle);
        scale.setFromX(0.3); scale.setFromY(0.3);
        scale.setToX(1.0); scale.setToY(1.0);
        scale.setInterpolator(Interpolator.EASE_OUT);
        scale.play();

        Label successTitle = new Label(title);
        successTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: 900; -fx-text-fill: white;");

        Label successSub = new Label(message);
        successSub.setStyle("-fx-font-size: 13px; -fx-text-fill: #8A95A3; -fx-text-alignment: center; -fx-wrap-text: true;");
        successSub.setMaxWidth(340);

        Button doneBtn = new Button(buttonText);
        doneBtn.setStyle(
                "-fx-background-color: " + color + "; -fx-text-fill: white;" +
                        "-fx-background-radius: 10; -fx-font-size: 14px; -fx-font-weight: bold;" +
                        "-fx-padding: 12 32; -fx-cursor: hand;");
        doneBtn.setOnAction(e -> {
            close();
            if (accepted && onSuccess != null) onSuccess.run();
        });

        FadeTransition fade = new FadeTransition(Duration.millis(500), content);
        fade.setFromValue(0); fade.setToValue(1);
        fade.setDelay(Duration.millis(200));
        fade.play();

        content.getChildren().addAll(checkCircle, successTitle, successSub, doneBtn);
        return content;
    }

    private VBox buildAcceptedContent(String title, String message, String color, EventService.ParticipationTicket ticket) {
        VBox content = new VBox(14);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(24, 30, 24, 30));
        content.setStyle("-fx-background-color: #0A0C0F;");

        StackPane qrFrame = new StackPane();
        qrFrame.setPadding(new Insets(10));
        qrFrame.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
        ImageView qrView = new ImageView(new Image(ticket.qrCodeUrl(), 180, 180, true, true, true));
        qrView.setFitWidth(180);
        qrView.setFitHeight(180);
        qrFrame.getChildren().add(qrView);

        Label successTitle = new Label(title);
        successTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: 900; -fx-text-fill: white;");

        Label successSub = new Label(message);
        successSub.setStyle("-fx-font-size: 13px; -fx-text-fill: #8A95A3; -fx-text-alignment: center; -fx-wrap-text: true;");
        successSub.setMaxWidth(340);

        Label codeLbl = new Label("Code: " + ticket.ticketCode());
        codeLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: " + color + "; -fx-font-weight: bold;");

        Button gmailBtn = new Button("Confirmer par Gmail");
        gmailBtn.setMaxWidth(Double.MAX_VALUE);
        gmailBtn.setStyle(
                "-fx-background-color: #1BBFA8; -fx-text-fill: white;" +
                        "-fx-background-radius: 10; -fx-font-size: 13px; -fx-font-weight: bold;" +
                        "-fx-padding: 11 0; -fx-cursor: hand;");
        gmailBtn.setOnAction(e -> openExternalUrl(ticket.gmailUrl()));

        Button doneBtn = new Button("Voir les details");
        doneBtn.setMaxWidth(Double.MAX_VALUE);
        doneBtn.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: #D0D5DD;" +
                        "-fx-border-color: #252830; -fx-border-radius: 10; -fx-background-radius: 10;" +
                        "-fx-font-size: 13px; -fx-padding: 10 0; -fx-cursor: hand;");
        doneBtn.setOnAction(e -> {
            close();
            if (onSuccess != null) onSuccess.run();
        });

        content.getChildren().addAll(successTitle, successSub, qrFrame, codeLbl, gmailBtn, doneBtn);
        return content;
    }

    private void openExternalUrl(String url) {
        try {
            if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                showError("Ouverture Gmail indisponible sur cette machine.");
                return;
            }
            Desktop.getDesktop().browse(URI.create(url));
        } catch (Exception ex) {
            showError("Impossible d'ouvrir Gmail : " + rootMessage(ex));
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setHeaderText(null);
        alert.getDialogPane().setStyle("-fx-background-color: #1C2028;");
        alert.showAndWait();
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
