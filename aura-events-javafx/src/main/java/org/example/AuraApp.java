package org.example;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.ui.MainLayout;

public class AuraApp extends Application {

    @Override
    public void start(Stage stage) {
        MainLayout root = new MainLayout();
        Scene scene = new Scene(root, 1200, 750);
        stage.setTitle("AURA - Events");
        stage.setScene(scene);
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}