package tn.esprit.aura.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.esprit.aura.entities.Event;
import tn.esprit.aura.services.EventService;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URL;

@SuppressWarnings("unused")
public class EventDetailController {

    @FXML private StackPane imagePane;
    @FXML private ImageView eventImage;
    @FXML private Label titleLabel, moodBadge, statusBadge;
    @FXML private Label dateValue, timeValue, locationValue;
    @FXML private Label participantsValue, priceValue, paymentValue, descriptionLabel, organizerLabel;
    @FXML private Label feedbackAverageLabel, feedbackTotalLabel, feedbackPositiveLabel, feedbackNegativeLabel, feedbackAnalysisLabel;
    @FXML private Label revenueTicketLabel, revenueParticipantsLabel, revenueTotalLabel, revenueDiscountLabel;
    @FXML private ProgressBar participantsBar;
    @FXML private Button editBtn, deleteBtn, mapsBtn;
    @FXML private VBox participantsList, feedbackList;
    @FXML private StackPane feedbackChartBox, revenueBarChartBox, revenuePieChartBox, revenueLineChartBox;

    private Event event;
    private EventService service;
    private int currentUserId;
    private Runnable onRefresh;

    public void setData(Event ev, EventService svc, int userId, Runnable refresh) {
        this.event = ev;
        this.service = svc;
        this.currentUserId = userId;
        this.onRefresh = refresh;
        populate();
    }

    private void populate() {
        // Image
        if (event.getImageUrl() != null && !event.getImageUrl().isBlank()) {
            try {
                Image img = new Image(event.getImageUrl(), 700, 240, true, true, true);
                eventImage.setImage(img);
            } catch (Exception e) {
                setPlaceholderImage();
            }
        } else {
            setPlaceholderImage();
        }

        // Titre
        titleLabel.setText(event.getTitle());

        // Mood badge
        String mood = event.getRecommendedMood();
        String mc = EventsViewController.moodColor(mood);
        moodBadge.setText(EventsViewController.moodLabel(mood));
        moodBadge.setStyle("-fx-background-color:" + mc + "cc; -fx-text-fill:white;" +
                "-fx-font-weight:bold; -fx-font-size:12px;" +
                "-fx-padding:4 12 4 12; -fx-background-radius:20;");

        // Statut
        EventService.EventAvailability availability = service.getEventAvailability(event.getIdEvent());
        int count = availability.confirmedParticipants();
        boolean full = availability.full();
        statusBadge.setText(full ? "Complet" : "Ouvert");
        statusBadge.setStyle("-fx-background-color:" + (full ? "#E85D3A44" : "#1BBFA844") +
                "; -fx-text-fill:" + (full ? "#E85D3A" : "#1BBFA8") +
                "; -fx-font-size:11px; -fx-padding:4 10 4 10; -fx-background-radius:20;");

        // Infos
        dateValue.setText(event.getEventDate() != null ? event.getEventDate().toString() : "-");
        timeValue.setText(event.getEventTime() != null ? event.getEventTime() : "-");
        locationValue.setText(event.getLocation() != null ? event.getLocation() : "-");
        if (priceValue != null) {
            priceValue.setText(formatPrice(event));
        }
        if (paymentValue != null) {
            paymentValue.setText(formatPayment(event));
        }
        if (mapsBtn != null) {
            mapsBtn.setDisable(event.getLocation() == null || event.getLocation().isBlank());
        }

        int max = availability.capacity();
        int rejected = service.getRejectedParticipantCount(event.getIdEvent());
        participantsValue.setText("Confirmes: " + count
                + " - Refuses: " + rejected
                + " - Places: " + availability.availableSpots() + "/" + max);
        participantsBar.setProgress(max == 0 ? 0 : (double) count / max);
        participantsBar.setStyle("-fx-accent: " + mc + ";");

        // Description
        descriptionLabel.setText(event.getDescription() != null && !event.getDescription().isBlank()
                ? event.getDescription() : "Aucune description.");

        organizerLabel.setText(buildOrganizerText());

        populateParticipants();
        populateRevenueDashboard(availability);
        populateFeedbackDashboard();
    }

    private void setPlaceholderImage() {
        imagePane.setStyle("-fx-background-color: linear-gradient(to bottom right, #1C2028, #0A0C0F);");
        Label placeholder = new Label("No image");
        placeholder.setStyle("-fx-font-size: 18px; -fx-text-fill: #5A6070;");
        imagePane.getChildren().add(0, placeholder);
    }

    @FXML
    private void onEdit() {
        if (event.getCreatedBy() != currentUserId) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Permission refusee");
            alert.setHeaderText("Action non autorisee");
            alert.setContentText("Vous ne pouvez modifier que vos propres evenements.");
            alert.getDialogPane().setStyle("-fx-background-color: #12151A;");
            Button okBtn = (Button) alert.getDialogPane().lookupButton(ButtonType.OK);
            okBtn.setStyle("-fx-background-color: #6B5FD4; -fx-text-fill: white; -fx-background-radius: 6;");
            alert.showAndWait();
            return;
        }

        try {
            Stage currentStage = (Stage) titleLabel.getScene().getWindow();
            URL fxmlUrl = getClass().getResource("/tn/esprit/aura/views/EventFormDialog.fxml");
            if (fxmlUrl == null) { System.err.println("EventFormDialog.fxml introuvable"); return; }
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
            EventFormController ctrl = loader.getController();
            ctrl.setData(event, service, () -> {
                if (onRefresh != null) onRefresh.run();
            });
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Modifier - " + event.getTitle());
            stage.setScene(new Scene(root));
            stage.setHeight(720);
            stage.show();
            currentStage.close();
        } catch (IOException ex) {
            System.err.println("Erreur : " + ex.getMessage());
        }
    }

    @FXML
    private void onDelete() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Supprimer l'evenement");
        confirm.setHeaderText(null);
        VBox confirmContent = new VBox(8);
        Label title = alertMessage("Supprimer \"" + event.getTitle() + "\" ?");
        Label body = alertMessage("Cette action est irreversible.");
        body.setStyle("-fx-text-fill: #D0D5DD; -fx-font-size: 13px;");
        confirmContent.getChildren().addAll(title, body);
        confirm.getDialogPane().setContent(confirmContent);
        confirm.getDialogPane().setStyle("-fx-background-color: #12151A;");

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
                service.deleteEvent(event.getIdEvent());
                if (onRefresh != null) onRefresh.run();
                close();
            }
        });
    }

    @FXML
    private void onClose() { close(); }

    @FXML
    private void onOpenMaps() {
        try {
            String url = service.buildGoogleMapsUrl(event.getLocation());
            if (url.isBlank()) {
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

    private void populateParticipants() {
        if (participantsList == null) {
            return;
        }
        participantsList.getChildren().clear();

        var participants = service.getParticipantRequests(event.getIdEvent());
        if (participants.isEmpty()) {
            Label empty = new Label("Aucun participant pour le moment.");
            empty.setStyle("-fx-font-size: 13px; -fx-text-fill: #8A95A3;");
            participantsList.getChildren().add(empty);
            return;
        }

        boolean canModerate = true;
        for (EventService.ParticipantRequest participant : participants) {
            participantsList.getChildren().add(buildParticipantRow(participant, canModerate));
        }
    }

    private HBox buildParticipantRow(EventService.ParticipantRequest participant, boolean canModerate) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10));
        row.setStyle("-fx-background-color: #12151A; -fx-background-radius: 8; -fx-border-color: #252830; -fx-border-radius: 8;");

        VBox info = new VBox(2);
        Label userLabel = new Label("Utilisateur #" + participant.userId());
        userLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #F0F2F5; -fx-font-weight: bold;");
        Label dateLabel = new Label(participant.registrationDate() == null ? "Date non disponible" : participant.registrationDate());
        dateLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #5A6070;");
        info.getChildren().addAll(userLabel, dateLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        boolean confirmed = isConfirmedStatus(participant.status());
        boolean rejected = isRejectedStatus(participant.status());

        Label statusBadge = new Label(statusLabel(participant.status()));
        statusBadge.setStyle(statusStyle(participant.status()));
        statusBadge.setMouseTransparent(true);

        Button acceptBtn = new Button("Accepter");
        acceptBtn.setStyle("-fx-background-color: #1BBFA822; -fx-border-color: #1BBFA8; -fx-border-radius: 6; -fx-background-radius: 6; -fx-text-fill: #1BBFA8; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 5 10; -fx-cursor: hand;");
        acceptBtn.setOnAction(e -> handleAcceptParticipant(participant.userId()));

        Button rejectBtn = new Button("Refuser");
        rejectBtn.setStyle("-fx-background-color: #E85D3A22; -fx-border-color: #E85D3A; -fx-border-radius: 6; -fx-background-radius: 6; -fx-text-fill: #E85D3A; -fx-font-size: 11px; -fx-padding: 5 10; -fx-cursor: hand;");
        rejectBtn.setOnAction(e -> handleRejectParticipant(participant.userId()));

        row.getChildren().addAll(info, spacer, statusBadge);
        if (canModerate) {
            if (!confirmed) {
                row.getChildren().add(acceptBtn);
            }
            if (!rejected) {
                row.getChildren().add(rejectBtn);
            }
        }
        return row;
    }

    private void handleAcceptParticipant(int userId) {
        try {
            boolean ok = service.acceptParticipant(event.getIdEvent(), userId);
            if (!ok) {
                showError("Acceptation impossible : les places sont insuffisantes.");
                return;
            }
            refreshAfterParticipantDecision();
            showInfo("Le participant a ete accepte avec succes.");
        } catch (RuntimeException ex) {
            showError("Acceptation impossible : " + rootMessage(ex));
        }
    }

    private void handleRejectParticipant(int userId) {
        try {
            boolean ok = service.rejectParticipant(event.getIdEvent(), userId);
            if (!ok) {
                showError("Refus impossible pour ce participant.");
                return;
            }
            refreshAfterParticipantDecision();
            showInfo("Le participant a ete refuse avec succes.");
        } catch (RuntimeException ex) {
            showError("Refus impossible : " + rootMessage(ex));
        }
    }

    private void refreshAfterParticipantDecision() {
        service.getEventById(event.getIdEvent()).ifPresent(fresh -> {
            this.event = fresh;
            populate();
        });
        if (onRefresh != null) {
            onRefresh.run();
        }
    }

    private String statusLabel(String status) {
        if (isConfirmedStatus(status)) return "Confirmer";
        if (isRejectedStatus(status)) return "Refuse";
        return "En attente";
    }

    private String statusStyle(String status) {
        String color = isConfirmedStatus(status) ? "#1BBFA8" : isRejectedStatus(status) ? "#E85D3A" : "#F59E0B";
        return "-fx-background-color: " + color + "22; -fx-border-color: " + color + ";"
                + "-fx-border-radius: 20; -fx-background-radius: 20;"
                + "-fx-text-fill: " + color + "; -fx-font-size: 11px; -fx-font-weight: bold;"
                + "-fx-padding: 4 10;";
    }

    private boolean isConfirmedStatus(String status) {
        String value = normalizeStatusText(status);
        if (isNegativeConfirmationStatus(value)) {
            return false;
        }
        return value.contains("confirm")
                || value.contains("accept")
                || value.contains("accepte")
                || value.contains("valid")
                || value.contains("approved")
                || value.contains("approuve");
    }

    private boolean isRejectedStatus(String status) {
        String value = normalizeStatusText(status);
        return value.contains("reject")
                || value.contains("refus")
                || value.contains("rejet")
                || value.contains("declin")
                || value.contains("denied")
                || value.contains("cancel")
                || value.contains("annul")
                || isNegativeConfirmationStatus(value);
    }

    private String normalizeStatusText(String status) {
        if (status == null) {
            return "";
        }
        return java.text.Normalizer.normalize(status.trim().toLowerCase(), java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
    }

    private boolean isNegativeConfirmationStatus(String normalized) {
        return normalized.contains("non confirme")
                || normalized.contains("not confirmed")
                || normalized.contains("unconfirmed");
    }

    private void populateFeedbackDashboard() {
        if (feedbackAverageLabel == null || feedbackList == null) {
            return;
        }

        EventService.FeedbackStats stats = service.getFeedbackStats(event.getIdEvent());
        feedbackAverageLabel.setText(stats.totalFeedbacks() == 0 ? "-" : String.format("%.1f/5", stats.averageRating()));
        feedbackTotalLabel.setText(String.valueOf(stats.totalFeedbacks()));
        feedbackPositiveLabel.setText(String.valueOf(stats.positiveCount()));
        feedbackNegativeLabel.setText(String.valueOf(stats.negativeCount()));
        if (feedbackAnalysisLabel != null) {
            feedbackAnalysisLabel.setText(buildFeedbackAnalysis(stats));
        }
        renderFeedbackPieChart(stats);

        feedbackList.getChildren().clear();
        var feedbacks = service.getFeedbacks(event.getIdEvent());
        if (feedbacks.isEmpty()) {
            Label empty = new Label("Aucun feedback pour cet evenement.");
            empty.setStyle("-fx-font-size: 13px; -fx-text-fill: #8A95A3;");
            feedbackList.getChildren().add(empty);
            return;
        }

        for (EventService.EventFeedback feedback : feedbacks) {
            feedbackList.getChildren().add(buildFeedbackRow(feedback));
        }
    }

    private void populateRevenueDashboard(EventService.EventAvailability availability) {
        if (revenueTicketLabel == null) {
            return;
        }

        int confirmed = availability.confirmedParticipants();
        double ticketPrice = event.getDiscountedPrice();
        double grossRevenue = event.getPrice() * confirmed;
        double revenue = ticketPrice * confirmed;
        double discountValue = Math.max(0, grossRevenue - revenue);

        revenueTicketLabel.setText(formatMoney(ticketPrice));
        revenueParticipantsLabel.setText(String.valueOf(confirmed));
        revenueTotalLabel.setText(formatMoney(revenue));
        revenueDiscountLabel.setText(formatMoney(discountValue));

        renderRevenueBarChart(ticketPrice, confirmed, revenue);
        renderRevenuePieChart(revenue, discountValue);
        renderRevenueLineChart(ticketPrice, confirmed);
    }

    private void renderRevenueBarChart(double ticketPrice, int confirmed, double revenue) {
        if (revenueBarChartBox == null) {
            return;
        }
        revenueBarChartBox.getChildren().clear();

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setLegendVisible(false);
        chart.setAnimated(false);
        chart.setCategoryGap(18);
        chart.setBarGap(6);
        chart.setPrefSize(260, 220);
        chart.setMaxSize(260, 220);
        chart.setStyle("-fx-background-color: transparent; -fx-font-size: 10px;");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.getData().add(new XYChart.Data<>("Ticket", ticketPrice));
        series.getData().add(new XYChart.Data<>("Participants", confirmed));
        series.getData().add(new XYChart.Data<>("Revenu", revenue));
        chart.getData().add(series);
        revenueBarChartBox.getChildren().add(chart);
    }

    private void renderRevenuePieChart(double revenue, double discountValue) {
        if (revenuePieChartBox == null) {
            return;
        }
        revenuePieChartBox.getChildren().clear();
        PieChart chart = new PieChart();
        chart.setLabelsVisible(true);
        chart.setLegendVisible(true);
        chart.setPrefSize(240, 220);
        chart.setMaxSize(240, 220);
        chart.setStyle("-fx-background-color: transparent; -fx-font-size: 10px;");
        chart.getData().add(new PieChart.Data("Revenu", Math.max(0, revenue)));
        chart.getData().add(new PieChart.Data("Reduction", Math.max(0, discountValue)));
        revenuePieChartBox.getChildren().add(chart);
    }

    private void renderRevenueLineChart(double ticketPrice, int confirmed) {
        if (revenueLineChartBox == null) {
            return;
        }
        revenueLineChartBox.getChildren().clear();

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        LineChart<String, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setLegendVisible(false);
        chart.setAnimated(false);
        chart.setCreateSymbols(true);
        chart.setPrefSize(300, 220);
        chart.setMaxSize(300, 220);
        chart.setStyle("-fx-background-color: transparent; -fx-font-size: 10px;");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        int step = Math.max(1, Math.max(confirmed, 5) / 5);
        for (int participants = 0; participants <= confirmed; participants += step) {
            series.getData().add(new XYChart.Data<>(String.valueOf(participants), ticketPrice * participants));
        }
        if (confirmed > 0 && (confirmed % step != 0)) {
            series.getData().add(new XYChart.Data<>(String.valueOf(confirmed), ticketPrice * confirmed));
        }
        chart.getData().add(series);
        revenueLineChartBox.getChildren().add(chart);
    }

    private VBox buildFeedbackRow(EventService.EventFeedback feedback) {
        VBox row = new VBox(6);
        row.setPadding(new Insets(10));
        row.setStyle("-fx-background-color: #12151A; -fx-background-radius: 8; -fx-border-color: #252830; -fx-border-radius: 8;");

        HBox top = new HBox(10);
        top.setAlignment(Pos.CENTER_LEFT);
        Label user = new Label("Utilisateur #" + feedback.userId());
        user.setStyle("-fx-font-size: 13px; -fx-text-fill: #F0F2F5; -fx-font-weight: bold;");
        Label rating = new Label(feedback.rating() + "/5");
        rating.setStyle("-fx-font-size: 12px; -fx-text-fill: " + ratingColor(feedback.rating()) + "; -fx-font-weight: bold;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label date = new Label(feedback.createdAt() == null ? "" : feedback.createdAt());
        date.setStyle("-fx-font-size: 11px; -fx-text-fill: #5A6070;");
        top.getChildren().addAll(user, rating, spacer, date);

        Label comment = new Label(feedback.comment() == null || feedback.comment().isBlank()
                ? "Aucun commentaire."
                : feedback.comment());
        comment.setWrapText(true);
        comment.setStyle("-fx-font-size: 12px; -fx-text-fill: #D0D5DD;");

        row.getChildren().addAll(top, comment);
        return row;
    }

    private void renderFeedbackPieChart(EventService.FeedbackStats stats) {
        if (feedbackChartBox == null) {
            return;
        }
        feedbackChartBox.getChildren().clear();
        if (stats.totalFeedbacks() == 0) {
            Label empty = new Label("Aucune donnee");
            empty.setStyle("-fx-font-size: 13px; -fx-text-fill: #8A95A3;");
            feedbackChartBox.getChildren().add(empty);
            return;
        }

        PieChart chart = new PieChart();
        chart.setLabelsVisible(true);
        chart.setLegendVisible(true);
        chart.setClockwise(true);
        chart.setStartAngle(90);
        chart.setPrefSize(250, 210);
        chart.setMaxSize(250, 210);
        chart.setStyle("-fx-background-color: transparent; -fx-font-size: 10px;");
        chart.getData().add(new PieChart.Data("Bons", stats.positiveCount()));
        chart.getData().add(new PieChart.Data("Neutres", stats.neutralCount()));
        chart.getData().add(new PieChart.Data("Mauvais", stats.negativeCount()));
        feedbackChartBox.getChildren().add(chart);
    }

    private String buildFeedbackAnalysis(EventService.FeedbackStats stats) {
        if (stats.totalFeedbacks() == 0) {
            return "Aucun avis n'a encore ete envoye pour cet evenement.";
        }

        double positiveRate = stats.positiveCount() * 100.0 / stats.totalFeedbacks();
        double negativeRate = stats.negativeCount() * 100.0 / stats.totalFeedbacks();
        String trend;
        if (positiveRate >= 70) {
            trend = "Les avis sont majoritairement bons.";
        } else if (negativeRate >= 40) {
            trend = "Les avis mauvais sont eleves et demandent une analyse des remarques.";
        } else {
            trend = "Les avis sont mixtes avec une satisfaction moyenne.";
        }

        return String.format(
                "%s Moyenne %.1f/5. Bons: %.0f%%, mauvais: %.0f%%. Total avis: %d.",
                trend,
                stats.averageRating(),
                positiveRate,
                negativeRate,
                stats.totalFeedbacks()
        );
    }

    private String ratingColor(int rating) {
        if (rating >= 4) return "#1BBFA8";
        if (rating == 3) return "#F59E0B";
        return "#E85D3A";
    }

    private String buildOrganizerText() {
        return service.getOrganizerById(event.getCreatedBy())
                .map(organizer -> {
                    String status = organizer.active() ? "" : " (inactif)";
                    String phone = organizer.phone() == null || organizer.phone().isBlank()
                            ? ""
                            : " - " + organizer.phone();
                    return organizer.fullName() + status + "\n" + organizer.email() + phone;
                })
                .orElse("Organisateur #" + event.getCreatedBy());
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

    private String formatMoney(double amount) {
        return String.format(java.util.Locale.US, "%.2f DT", amount);
    }

    private void close() {
        ((Stage) titleLabel.getScene().getWindow()).close();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, "", ButtonType.OK);
        alert.setHeaderText(null);
        alert.getDialogPane().setStyle("-fx-background-color: #12151A;");
        alert.getDialogPane().setContent(alertMessage(message));
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "", ButtonType.OK);
        alert.setHeaderText(null);
        alert.getDialogPane().setStyle("-fx-background-color: #12151A;");
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
