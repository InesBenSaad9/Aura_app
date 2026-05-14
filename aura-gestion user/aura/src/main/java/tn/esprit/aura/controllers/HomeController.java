package tn.esprit.aura.controllers;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import tn.esprit.aura.utils.NavigationManager;

public class HomeController {

    @FXML private Circle orb1;
    @FXML private Circle orb2;
    @FXML private Circle orb3;

    @FXML private HBox navbar;
    @FXML private HBox heroBadge;
    @FXML private Label heroTitle;
    @FXML private Label heroSubtitle;
    @FXML private Label heroDescription;
    @FXML private HBox tagsBox;
    @FXML private VBox ctaBox;
    @FXML private HBox footerBox;
    @FXML private ScrollPane homeScroll;
    @FXML private VBox showcaseSection;
    @FXML private HBox featuresSection;
    @FXML private ImageView homeHeroImage;

    @FXML
    public void initialize() {
        // --- Continuous Orb Animations (Floating effect) ---
        animateOrb(orb1, 40, 30, 8000);
        animateOrb(orb2, -30, 50, 10000);
        animateOrb(orb3, 50, -40, 12000);

        // --- Entrance Animations (Fade In + Slide Up) ---
        // Hide elements initially
        Node[] elements = {navbar, heroBadge, heroTitle, heroSubtitle, heroDescription, tagsBox, ctaBox, footerBox};
        for (Node node : elements) {
            node.setOpacity(0);
            node.setTranslateY(30);
        }

        // Sequential transition
        SequentialTransition seqT = new SequentialTransition();
        int delay = 0;
        
        for (Node node : elements) {
            FadeTransition ft = new FadeTransition(Duration.millis(600), node);
            ft.setToValue(1);
            
            TranslateTransition tt = new TranslateTransition(Duration.millis(600), node);
            tt.setToY(0);
            tt.setInterpolator(Interpolator.EASE_OUT);
            
            ParallelTransition pt = new ParallelTransition(ft, tt);
            pt.setDelay(Duration.millis(delay));
            
            seqT.getChildren().add(pt);
            delay += 100; // Stagger effect
        }
        
        seqT.play();

        // Load a premium web image in the home page.
        homeHeroImage.setImage(new Image(
                "https://images.unsplash.com/photo-1518770660439-4636190af475?auto=format&fit=crop&w=1600&q=80",
                true
        ));

        // Keep added sections hidden initially and animate on first scroll.
        showcaseSection.setOpacity(0);
        showcaseSection.setTranslateY(40);
        featuresSection.setOpacity(0);
        featuresSection.setTranslateY(40);

        homeScroll.vvalueProperty().addListener((obs, oldV, newV) -> {
            double progress = newV.doubleValue();
            if (progress > 0.12 && showcaseSection.getOpacity() == 0) {
                animateSection(showcaseSection);
            }
            if (progress > 0.22 && featuresSection.getOpacity() == 0) {
                animateSection(featuresSection);
            }
            // Slight parallax on hero title while scrolling.
            heroTitle.setTranslateY(-progress * 30);
            heroSubtitle.setTranslateY(-progress * 20);
        });
    }

    private void animateSection(Node node) {
        FadeTransition fade = new FadeTransition(Duration.millis(600), node);
        fade.setToValue(1);
        TranslateTransition slide = new TranslateTransition(Duration.millis(600), node);
        slide.setToY(0);
        slide.setInterpolator(Interpolator.EASE_OUT);
        new ParallelTransition(fade, slide).play();
    }

    private void animateOrb(Circle orb, double byX, double byY, double durationMs) {
        TranslateTransition tt = new TranslateTransition(Duration.millis(durationMs), orb);
        tt.setByX(byX);
        tt.setByY(byY);
        tt.setAutoReverse(true);
        tt.setCycleCount(Animation.INDEFINITE);
        tt.setInterpolator(Interpolator.EASE_BOTH);
        tt.play();
    }

    @FXML
    private void handleGetStarted() {
        NavigationManager.navigateTo("/tn/esprit/aura/views/Login.fxml");
    }

    @FXML
    private void handleLogin() {
        NavigationManager.navigateTo("/tn/esprit/aura/views/Login.fxml");
    }

    @FXML
    private void handleRegister() {
        NavigationManager.navigateTo("/tn/esprit/aura/views/Register.fxml");
    }
}
