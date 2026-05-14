package tn.esprit.aura.controllers;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import tn.esprit.aura.entities.User;
import tn.esprit.aura.services.UserService;
import tn.esprit.aura.utils.FaceIdService;
import tn.esprit.aura.utils.NavigationManager;
import tn.esprit.aura.utils.SessionManager;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class FaceIdAuthController {

    @FXML private ImageView webcamView;
    @FXML private Label statusLabel;
    @FXML private Button btnCapture;
    @FXML private ProgressIndicator loadingIndicator;

    private Webcam webcam;
    private AtomicBoolean running = new AtomicBoolean(false);
    private final UserService userService = new UserService();

    @FXML
    public void initialize() {
        // Automatically stop webcam if user navigates away
        webcamView.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene == null) {
                stopWebcam();
            }
        });

        Thread t = new Thread(this::initWebcam);
        t.setDaemon(true);
        t.start();
    }

    private void initWebcam() {
        running.set(true);
        try {
            List<Webcam> webcams = Webcam.getWebcams();
            if (webcams.isEmpty()) {
                Platform.runLater(() -> statusLabel.setText("Aucune webcam detectee."));
                return;
            }
            
            if (!running.get()) return; // Navigation happened during camera search

            webcam = webcams.get(0);
            webcam.setViewSize(WebcamResolution.VGA.getSize());
            webcam.open();

            if (!running.get()) { // Navigation happened during camera open
                webcam.close();
                return;
            }
            Platform.runLater(() -> {
                btnCapture.setDisable(false);
                statusLabel.setText("Regardez la camera puis cliquez sur Verifier");
            });

            while (running.get()) {
                BufferedImage image = webcam.getImage();
                if (image != null) {
                    WritableImage fxImage = SwingFXUtils.toFXImage(image, null);
                    Platform.runLater(() -> webcamView.setImage(fxImage));
                }
                Thread.sleep(50);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Platform.runLater(() -> statusLabel.setText("Erreur camera : " + e.getMessage()));
        }
    }

    @FXML
    private void handleCapture() {
        if (webcam == null || !webcam.isOpen()) return;

        btnCapture.setDisable(true);
        loadingIndicator.setVisible(true);
        statusLabel.setText("Extraction des caracteristiques du visage...");

        BufferedImage image = webcam.getImage();
        
        Thread authThread = new Thread(() -> {
            try {
                // Convert to bytes
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(image, "jpg", baos);
                byte[] imageBytes = baos.toByteArray();

                // Get embeddings from Hugging Face
                double[] currentEmbeddings = FaceIdService.getEmbeddings(imageBytes);

                // Find matching user in database
                List<User> users = userService.getAll();
                User matchedUser = null;
                double maxSimilarity = -1;

                for (User user : users) {
                    if (user.getFaceData() != null) {
                        double[] savedEmbeddings = FaceIdService.stringToEmbeddings(user.getFaceData());
                        double similarity = FaceIdService.calculateCosineSimilarity(currentEmbeddings, savedEmbeddings);
                        
                        // 0.85 is a good threshold for CLIP embeddings
                        if (similarity > 0.85 && similarity > maxSimilarity) {
                            maxSimilarity = similarity;
                            matchedUser = user;
                        }
                    }
                }

                if (matchedUser != null) {
                    User finalMatchedUser = matchedUser;
                    Platform.runLater(() -> {
                        statusLabel.setText("Identite verifiee : " + finalMatchedUser.getFullName());
                        tn.esprit.aura.utils.SessionManager.setCurrentUser(finalMatchedUser);
                        stopWebcam();
                        if (finalMatchedUser.isAdmin()) {
                            NavigationManager.navigateTo("/tn/esprit/aura/views/Admin.fxml");
                        } else {
                            NavigationManager.navigateTo("/tn/esprit/aura/views/UserDashboard.fxml");
                        }
                    });
                } else {
                    Platform.runLater(() -> {
                        statusLabel.setText("Acces refuse : visage non reconnu.");
                        btnCapture.setDisable(false);
                        loadingIndicator.setVisible(false);
                    });
                }

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    statusLabel.setText("Erreur du service : " + e.getMessage());
                    btnCapture.setDisable(false);
                    loadingIndicator.setVisible(false);
                });
            }
        });
        authThread.setDaemon(true);
        authThread.start();
    }

    @FXML
    private void handleCancel() {
        stopWebcam();
        NavigationManager.navigateTo("/tn/esprit/aura/views/Login.fxml");
    }

    private void stopWebcam() {
        running.set(false);
        if (webcam != null) {
            webcam.close();
        }
    }
}
