package com.bit.examsystem.student.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Callback;
import java.io.IOException;

public class ViewManager {
    private static Stage primaryStage;
    private static Callback<Class<?>, Object> controllerFactory;

    public static void init(Stage stage, Callback<Class<?>, Object> factory) {
        primaryStage = stage;
        controllerFactory = factory;
    }

    public static void switchScene(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(ViewManager.class.getResource(fxmlPath));
            loader.setControllerFactory(controllerFactory);
            Scene scene = new Scene(loader.load());
            primaryStage.setTitle(title);
            primaryStage.setScene(scene);
            primaryStage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
