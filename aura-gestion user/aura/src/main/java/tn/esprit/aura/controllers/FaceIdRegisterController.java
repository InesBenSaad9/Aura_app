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

public class FaceIdRegisterController {
    
    public static java.util.function.Consumer<String> onFaceCaptured;
    public static String returnFxmlPath = "/tn/esprit/aura/views/Profile.fxml";

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
                statusLabel.setText("Regardez la camera puis cliquez sur Enregistrer");
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
        }
    }

    @FXML
    private void handleRegister() {
        User currentUser = tn.esprit.aura.utils.SessionManager.getCurrentUser();
        // If no user is logged in, we are likely in the registration flow

        btnCapture.setDisable(true);
        loadingIndicator.setVisible(true);
        statusLabel.setText("Generation de la signature biometrique...");

        BufferedImage image = webcam.getImage();
        
        Thread captureThread = new Thread(() -> {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(image, "jpg", baos);
                byte[] imageBytes = baos.toByteArray();

                double[] embeddings = FaceIdService.getEmbeddings(imageBytes);
                String embeddingString = FaceIdService.embeddingsToString(embeddings);

                if (currentUser != null) {
                    currentUser.setFaceData(embeddingString);
                    userService.update(currentUser);
                }

                if (onFaceCaptured != null) {
                    onFaceCaptured.accept(embeddingString);
                }

                Platform.runLater(() -> {
                    statusLabel.setText("Visage capture avec succes !");
                    stopWebcam();
                    javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(1));
                    pause.setOnFinished(e -> {
                        if (returnFxmlPath != null) {
                            NavigationManager.navigateTo(returnFxmlPath);
                        } else {
                            ((javafx.stage.Stage) webcamView.getScene().getWindow()).close();
                        }
                    });
                    pause.play();
                });

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    statusLabel.setText("Erreur d enregistrement : " + e.getMessage());
                    btnCapture.setDisable(false);
                    loadingIndicator.setVisible(false);
                });
            }
        });
        captureThread.setDaemon(true);
        captureThread.start();
    }

    @FXML
    private void handleCancel() {
        stopWebcam();
        if (returnFxmlPath != null) {
            NavigationManager.navigateTo(returnFxmlPath);
        } else {
            ((javafx.stage.Stage) webcamView.getScene().getWindow()).close();
        }
    }

    private void stopWebcam() {
        running.set(false);
        if (webcam != null) {
            webcam.close();
        }
    }
}
