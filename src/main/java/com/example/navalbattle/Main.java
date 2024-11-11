package com.example.navalbattle;

import com.example.navalbattle.controller.NavalController;
import com.example.navalbattle.model.NavalModel;
import com.example.navalbattle.view.NavalView;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Clase principal para inicializar la aplicación.
 */
public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        NavalModel model = new NavalModel();
        NavalView view = new NavalView(primaryStage);
        new NavalController(model, view);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
