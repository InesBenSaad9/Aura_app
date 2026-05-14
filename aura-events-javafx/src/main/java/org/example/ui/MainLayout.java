package org.example.ui;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import org.example.ui.events.UserEventsView;

import java.net.URL;

public class MainLayout extends HBox {

    private VBox sidebar;
    private StackPane contentArea;
    private Role currentRole;

    private enum Role {
        USER,
        ORGANIZER
    }

    public static final int CURRENT_USER_ID = 1;
    public static final String CURRENT_USER_NAME = "Ines";

    // Palette
    private static final String PRIMARY   = "#1BBFA8";
    private static final String SECONDARY = "#6B5FD4";
    private static final String TERTIARY  = "#E85D3A";
    private static final String NEUTRAL   = "#0A0C0F";
    private static final String CARD_BG   = "#12151A";
    private static final String SURFACE   = "#1C2028";
    private static final String BORDER    = "#252830";

    public MainLayout() {
        super();
        setStyle("-fx-background-color: " + NEUTRAL + ";");
        buildContent();
        showLoginChoice();
    }

    private void buildSidebar() {
        sidebar = new VBox(0);
        sidebar.setStyle(
                "-fx-background-color: " + CARD_BG + ";" +
                        "-fx-border-color: " + BORDER + ";" +
                        "-fx-border-width: 0 1 0 0;"
        );
        sidebar.setPrefWidth(210);
        sidebar.setMinWidth(210);
        sidebar.setPadding(new Insets(24, 0, 24, 0));

        //  Logo 
        HBox logo = new HBox(12);
        logo.setAlignment(Pos.CENTER_LEFT);
        logo.setPadding(new Insets(0, 20, 28, 20));

        VBox logoLabels = new VBox(2);
        Label appName = new Label("AURA");
        appName.setStyle(
                "-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white;" +
                        "-fx-font-family: 'Segoe UI';"
        );
        Label appSub = new Label(currentRole == Role.ORGANIZER ? "Event Organizer" : "AI Life Companion");
        appSub.setStyle("-fx-font-size: 11px; -fx-text-fill: #5a6070;");
        logoLabels.getChildren().addAll(appName, appSub);
        logo.getChildren().add(logoLabels);

        //  Separator 
        HBox sep1 = separator();

        //  Profile 
        HBox profile = new HBox(12);
        profile.setAlignment(Pos.CENTER_LEFT);
        profile.setPadding(new Insets(16, 20, 20, 20));

        StackPane avatar = new StackPane();
        avatar.setPrefSize(42, 42);
        avatar.setMinSize(42, 42);
        avatar.setStyle(
                "-fx-background-color: " + PRIMARY + ";" +
                        "-fx-background-radius: 21;"
        );
        Label initials = new Label("IN");
        initials.setStyle(
                "-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: white;"
        );
        avatar.getChildren().add(initials);

        VBox userInfo = new VBox(3);
        Label userName = new Label(CURRENT_USER_NAME);
        userName.setStyle(
                "-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #f0f2f5;"
        );
        Label userStatus = new Label(currentRole == Role.ORGANIZER ? "ORGANIZER" : "ACTIVE");
        userStatus.setStyle(
                "-fx-font-size: 10px; -fx-text-fill: " + PRIMARY + "; -fx-font-weight: bold;"
        );
        userInfo.getChildren().addAll(userName, userStatus);
        profile.getChildren().addAll(avatar, userInfo);

        //  Separator 
        HBox sep2 = separator();

        //  Nav 
        VBox nav = new VBox(2);
        nav.setPadding(new Insets(12, 10, 12, 10));
        HBox eventsItem = navItemActive("Events");
        eventsItem.setOnMouseClicked(e -> loadHomeForCurrentRole());

        nav.getChildren().addAll(
                navItem("Tasks", false),
                navItem("AI Analysis", false),
                navItem("Activities", false),
                navItem("Medical", false),
                navItem("Messages", false),
                eventsItem,
                navItem("Profile", false)
        );

        //  Spacer 
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        //  Bottom 
        HBox sep3 = separator();

        VBox bottom = new VBox(2);
        bottom.setPadding(new Insets(8, 10, 0, 10));
        bottom.getChildren().addAll(
                navItem("Settings", false),
                logoutItem()
        );

        sidebar.getChildren().addAll(
                logo, sep1, profile, sep2, nav, spacer, sep3, bottom
        );
    }

    private HBox separator() {
        HBox sep = new HBox();
        sep.setPrefHeight(1);
        sep.setMinHeight(1);
        sep.setStyle("-fx-background-color: " + BORDER + ";");
        return sep;
    }
    private HBox navItem(String label, boolean active) {
        HBox item = new HBox(0);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(10, 14, 10, 14));
        item.setMaxWidth(Double.MAX_VALUE);

        Label textLbl = new Label(label);
        textLbl.setStyle("-fx-font-size: 14px; -fx-text-fill: #8a95a3;");
        item.setStyle("-fx-background-radius: 10; -fx-cursor: hand;");

        item.setOnMouseEntered(e -> {
            item.setStyle("-fx-background-color: " + SURFACE + "; -fx-background-radius: 10; -fx-cursor: hand;");
            textLbl.setStyle("-fx-font-size: 14px; -fx-text-fill: #d0d5dd;");
        });
        item.setOnMouseExited(e -> {
            item.setStyle("-fx-background-radius: 10; -fx-cursor: hand;");
            textLbl.setStyle("-fx-font-size: 14px; -fx-text-fill: #8a95a3;");
        });

        item.getChildren().add(textLbl);
        return item;
    }
    private HBox navItemActive(String label) {
        HBox item = new HBox(0);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(10, 14, 10, 14));
        item.setMaxWidth(Double.MAX_VALUE);
        item.setStyle(
                "-fx-background-color: " + PRIMARY + "1A;" +
                        "-fx-border-color: " + PRIMARY + ";" +
                        "-fx-border-width: 0 0 0 3;" +
                        "-fx-background-radius: 0 10 10 0;" +
                        "-fx-cursor: hand;"
        );

        Label textLbl = new Label(label);
        textLbl.setStyle(
                "-fx-font-size: 14px; -fx-text-fill: " + PRIMARY + "; -fx-font-weight: bold;"
        );

        item.getChildren().add(textLbl);
        return item;
    }

    private HBox logoutItem() {
        HBox item = new HBox(12);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(10, 14, 10, 14));
        item.setMaxWidth(Double.MAX_VALUE);
        item.setStyle("-fx-background-radius: 10; -fx-cursor: hand;");
        Label textLbl = new Label("Logout");
        textLbl.setStyle("-fx-font-size: 14px; -fx-text-fill: " + TERTIARY + ";");

        item.setOnMouseEntered(e -> item.setStyle(
                "-fx-background-color: " + TERTIARY + "22;" +
                        "-fx-background-radius: 10; -fx-cursor: hand;"
        ));
        item.setOnMouseExited(e -> item.setStyle(
                "-fx-background-radius: 10; -fx-cursor: hand;"
        ));
        item.setOnMouseClicked(e -> showLoginChoice());

        item.getChildren().add(textLbl);
        return item;
    }

    private void buildContent() {
        contentArea = new StackPane();
        contentArea.getStyleClass().add("content-area");
        contentArea.setStyle("-fx-background-color: " + NEUTRAL + ";");
    }

    private void showLoginChoice() {
        currentRole = null;
        getChildren().clear();
        contentArea.getChildren().setAll(buildLoginChoice());
        getChildren().add(contentArea);
        HBox.setHgrow(contentArea, Priority.ALWAYS);
    }

    private VBox buildLoginChoice() {
        VBox page = new VBox(24);
        page.setAlignment(Pos.CENTER);
        page.setPadding(new Insets(48));
        page.setStyle("-fx-background-color: " + NEUTRAL + ";");

        VBox titleBlock = new VBox(8);
        titleBlock.setAlignment(Pos.CENTER);
        Label title = new Label("AURA");
        title.setStyle("-fx-font-size: 34px; -fx-font-weight: 900; -fx-text-fill: white;");
        Label subtitle = new Label("Choose how you want to login");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #8A95A3;");
        titleBlock.getChildren().addAll(title, subtitle);

        HBox choices = new HBox(16);
        choices.setAlignment(Pos.CENTER);

        Button organizerBtn = loginButton("Login as event organizer", SECONDARY);
        organizerBtn.setOnAction(e -> login(Role.ORGANIZER));

        Button userBtn = loginButton("Login as user", PRIMARY);
        userBtn.setOnAction(e -> login(Role.USER));

        choices.getChildren().addAll(organizerBtn, userBtn);
        page.getChildren().addAll(titleBlock, choices);
        return page;
    }

    private Button loginButton(String text, String color) {
        Button button = new Button(text);
        button.setMinWidth(230);
        button.setMinHeight(52);
        button.setStyle(
                "-fx-background-color: " + color + "; -fx-text-fill: white;" +
                        "-fx-font-size: 14px; -fx-font-weight: bold;" +
                        "-fx-background-radius: 10; -fx-cursor: hand;");
        return button;
    }

    private void login(Role role) {
        currentRole = role;
        getChildren().clear();
        buildSidebar();
        getChildren().addAll(sidebar, contentArea);
        HBox.setHgrow(contentArea, Priority.ALWAYS);
        loadHomeForCurrentRole();
    }

    private void loadHomeForCurrentRole() {
        if (currentRole == Role.ORGANIZER) {
            loadEventsView();
        } else {
            contentArea.getChildren().setAll(new UserEventsView());
        }
    }

    public void loadEventsView() {
        try {
            URL fxmlUrl = getClass().getResource("/EventsView.fxml");
            if (fxmlUrl == null) { showError("EventsView.fxml introuvable"); return; }
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent view = loader.load();
            contentArea.getChildren().setAll(view);
        } catch (Exception ex) {
            showError("Erreur : " + rootMessage(ex));
        }
    }

    private void showError(String msg) {
        Label err = new Label(msg);
        err.setStyle("-fx-text-fill: " + TERTIARY + "; -fx-font-size: 14px;");
        contentArea.getChildren().setAll(err);
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
