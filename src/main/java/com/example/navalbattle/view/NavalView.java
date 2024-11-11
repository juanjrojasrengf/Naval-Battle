package com.example.navalbattle.view;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Stage;

/**
 * Vista para el juego Batalla Naval.
 */
public class NavalView {
    private final GridPane playerBoardView;
    private final GridPane mainBoardView;
    private final Button playButton;
    private final Button showMachineBoardButton;
    private final Label messageLabel;

    public NavalView(Stage stage) {
        playerBoardView = new GridPane();
        mainBoardView = new GridPane();
        playButton = new Button("Jugar");
        showMachineBoardButton = new Button("Mostrar Tablero Máquina");
        messageLabel = new Label();

        Label playerBoardLabel = new Label("Tablero de Posición");
        Label mainBoardLabel = new Label("Tablero Principal");

        HBox centerLayout = new HBox(20,
                new VBox(10, playerBoardView, playerBoardLabel),
                new VBox(10, mainBoardView, mainBoardLabel)
        );

        BorderPane root = new BorderPane();
        root.setCenter(centerLayout);
        root.setBottom(new VBox(10, new HBox(20, playButton, showMachineBoardButton), messageLabel));
        BorderPane.setAlignment(centerLayout, Pos.CENTER);

        configureBoard(playerBoardView);
        configureBoard(mainBoardView);

        Scene scene = new Scene(root, 1000, 700);
        String stylesheet = getClass().getResource("/com/example/navalbattle/style.css") != null
                ? getClass().getResource("/com/example/navalbattle/style.css").toExternalForm()
                : null;

        if (stylesheet != null) {
            scene.getStylesheets().add(stylesheet);
        } else {
            System.out.println("Warning: CSS file not found.");
        }

        stage.setScene(scene);
        stage.setTitle("Batalla Naval");
        stage.show();
    }

    private void configureBoard(GridPane board) {
        board.setGridLinesVisible(true);
        for (int i = 0; i < 11; i++) {
            for (int j = 0; j < 11; j++) {
                Label cell = createCell(i, j);
                board.add(cell, j, i);
            }
        }
    }

    private Label createCell(int i, int j) {
        Label cell = new Label();
        cell.setMinSize(40, 40);
        cell.setStyle("-fx-border-color: black; -fx-alignment: center;");

        if (i == 0 && j > 0) {
            cell.setText(String.valueOf(j - 1));
            cell.setStyle("-fx-background-color: white; -fx-alignment: center;");
        } else if (j == 0 && i > 0) {
            cell.setText(String.valueOf((char) ('A' + i - 1)));
            cell.setStyle("-fx-background-color: white; -fx-alignment: center;");
        } else if (i > 0 && j > 0) {
            cell.setStyle("-fx-background-color: blue; -fx-border-color: black;");
        }

        return cell;
    }

    public Button getPlayButton() {
        return playButton;
    }

    public Button getShowMachineBoardButton() {
        return showMachineBoardButton;
    }

    public Label getMessageLabel() {
        return messageLabel;
    }

    public GridPane getPlayerBoardView() {
        return playerBoardView;
    }

    public GridPane getMainBoardView() {
        return mainBoardView;
    }
}
