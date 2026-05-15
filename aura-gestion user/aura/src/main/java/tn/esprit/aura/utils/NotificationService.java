package tn.esprit.aura.utils;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class NotificationService {

    public enum Type {
        MESSAGE     ("💬", "#00b4ab"),
        APPOINTMENT ("📅", "#f0a500"),
        PRESCRIPTION("💊", "#7c3aed"),
        SUCCESS     ("✅", "#22c55e"),
        ERROR       ("❌", "#ef4444");

        public final String icon;
        public final String color;
        Type(String i, String c) { icon = i; color = c; }
    }

    private static NotificationService instance;
    public static NotificationService getInstance() {
        if (instance == null) instance = new NotificationService();
        return instance;
    }
    private NotificationService() {}

    private int stackOffset = 0;

    public static void message(String from, String text) {
        getInstance().show(Type.MESSAGE, "Message de " + from, text);
    }
    public static void appointmentAccepted(String doctor, String date) {
        getInstance().show(Type.APPOINTMENT, "Séance confirmée !", "Dr. " + doctor + " · " + date);
    }
    public static void appointmentRefused(String doctor) {
        getInstance().show(Type.APPOINTMENT, "Séance refusée", "Dr. " + doctor + " a refusé");
    }
    public static void appointmentRequest(String patient) {
        getInstance().show(Type.APPOINTMENT, "Nouvelle demande", patient + " demande une séance");
    }
    public static void prescription(String med, String patient) {
        getInstance().show(Type.PRESCRIPTION, "Prescription créée", med + " → " + patient);
    }
    public static void success(String msg) {
        getInstance().show(Type.SUCCESS, "Succès", msg);
    }
    public static void error(String msg) {
        getInstance().show(Type.ERROR, "Erreur", msg);
    }

    public void show(Type type, String title, String body) {
        Platform.runLater(() -> {
            try {
                Stage notifStage = new Stage();
                notifStage.initStyle(StageStyle.TRANSPARENT);
                notifStage.setAlwaysOnTop(true);
                notifStage.setResizable(false);

                HBox card = buildCard(type, title, body, notifStage);
                card.setOpacity(0);
                card.setTranslateX(380);

                StackPane root = new StackPane(card);
                root.setStyle("-fx-background-color: transparent;");
                root.setPrefSize(355, 82);

                Scene scene = new Scene(root, 355, 82);
                scene.setFill(Color.TRANSPARENT);
                notifStage.setScene(scene);

                Rectangle2D screen = Screen.getPrimary().getVisualBounds();
                notifStage.setX(screen.getMaxX() - 365);
                notifStage.setY(screen.getMaxY() - 98 - (stackOffset * 94));
                stackOffset++;

                notifStage.show();

                TranslateTransition slide = new TranslateTransition(Duration.millis(350), card);
                slide.setFromX(380);
                slide.setToX(0);
                slide.setInterpolator(Interpolator.EASE_OUT);

                FadeTransition fadeIn = new FadeTransition(Duration.millis(350), card);
                fadeIn.setFromValue(0);
                fadeIn.setToValue(1);

                new ParallelTransition(slide, fadeIn).play();

                FadeTransition fadeOut = new FadeTransition(Duration.millis(500), card);
                fadeOut.setDelay(Duration.millis(4000));
                fadeOut.setFromValue(1);
                fadeOut.setToValue(0);
                fadeOut.setOnFinished(e -> {
                    notifStage.close();
                    stackOffset = Math.max(0, stackOffset - 1);
                });
                fadeOut.play();

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private HBox buildCard(Type type, String title, String body, Stage ns) {
        VBox stripe = new VBox();
        stripe.setPrefWidth(5);
        stripe.setMinWidth(5);
        stripe.setMaxWidth(5);
        stripe.setStyle("-fx-background-color: " + type.color + ";");

        Label iconL = new Label(type.icon);
        iconL.setStyle("-fx-font-size: 20px;");

        Label titleL = new Label(title);
        titleL.setStyle("-fx-text-fill: #e6edf3; -fx-font-size: 12px; -fx-font-weight: bold;");
        titleL.setMaxWidth(200);

        Label bodyL = new Label(body);
        bodyL.setStyle("-fx-text-fill: #8b949e; -fx-font-size: 11px;");
        bodyL.setWrapText(true);
        bodyL.setMaxWidth(200);

        VBox texts = new VBox(2, titleL, bodyL);
        texts.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(texts, Priority.ALWAYS);

        Label closeL = new Label("✕");
        closeL.setStyle("-fx-text-fill: #8b949e; -fx-font-size: 11px; -fx-cursor: hand;");
        closeL.setOnMouseClicked(e -> {
            ns.close();
            stackOffset = Math.max(0, stackOffset - 1);
        });

        HBox inner = new HBox(10, iconL, texts, closeL);
        inner.setAlignment(Pos.CENTER_LEFT);
        inner.setPadding(new Insets(10, 12, 10, 12));
        HBox.setHgrow(inner, Priority.ALWAYS);

        HBox card = new HBox(stripe, inner);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPrefWidth(350);
        card.setPrefHeight(80);
        card.setStyle(cardStyle(false));

        card.setOnMouseEntered(e -> card.setStyle(cardStyle(true)));
        card.setOnMouseExited(e  -> card.setStyle(cardStyle(false)));
        card.setOnMouseClicked(e -> {
            ns.close();
            stackOffset = Math.max(0, stackOffset - 1);
        });

        return card;
    }

    private String cardStyle(boolean hover) {
        return "-fx-background-color: " + (hover ? "#252d3a" : "#1e2530") + ";" +
                "-fx-background-radius: 8;" +
                "-fx-border-color: #30363d;" +
                "-fx-border-width: 1;" +
                "-fx-border-radius: 8;" +
                "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.7),16,0,0,4);";
    }
}
