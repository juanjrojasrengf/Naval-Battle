package com.example.navalbattle.controller;

import com.example.navalbattle.model.NavalModel;
import com.example.navalbattle.view.NavalView;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Random;
import javafx.scene.control.TextInputDialog;
import java.util.Optional;

/**
 * Controlador que gestiona la lógica de la Batalla Naval.
 */
public class NavalController {
    private NavalModel model;
    private final NavalView view;

    public NavalController(NavalModel model, NavalView view) {
        this.model = model;
        this.view = view;

        setupPlayButton(); // Configurar botón "Jugar"
        setupShowMachineBoardButton(); // Configurar botón "Mostrar Tablero Máquina"
        showInitialDialog(); // Mostrar el diálogo inicial
        setupMainBoardInteraction();
        renderPlayerBoard();
    }

    private void showInitialDialog() {
        Stage dialog = new Stage();
        VBox dialogVBox = new VBox(10);
        dialogVBox.setAlignment(javafx.geometry.Pos.CENTER);

        Button loadGameButton = new Button("Continuar Juego Guardado");
        Button newGameButton = new Button("Iniciar Nuevo Juego");

        loadGameButton.setOnAction(event -> {
            try {
                loadGameState(); // Cargar el estado guardado
                dialog.close();
            } catch (IOException | ClassNotFoundException e) {
                view.getMessageLabel().setText("No se encontró un juego guardado. Iniciando nuevo juego.");
                model.startGame();
                dialog.close();
            }
        });

        newGameButton.setOnAction(event -> {
            promptForPlayerName(); // Solicitar nombre del jugador
            model.startGame();
            dialog.close();
        });

        dialogVBox.getChildren().addAll(new Label("¿Qué deseas hacer?"), loadGameButton, newGameButton);
        Scene dialogScene = new Scene(dialogVBox, 300, 150);
        dialog.setScene(dialogScene);
        dialog.showAndWait();
    }

    private void promptForPlayerName() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Nombre del Jugador");
        dialog.setHeaderText("Por favor, ingresa tu nombre:");
        dialog.setContentText("Nombre:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            model.setPlayerName(name);
            try {
                model.savePlayerStats("playerStats.txt");
            } catch (IOException e) {
                view.getMessageLabel().setText("Error al guardar el nombre del jugador.");
            }
        });
    }

    private void setupPlayButton() {
        view.getPlayButton().setOnAction(e -> {
            model.startGame();
            view.getMessageLabel().setText("El juego ha comenzado.");
            view.getPlayerBoardView().setDisable(true); // Bloquear tablero de posición
        });
    }

    private void setupShowMachineBoardButton() {
        view.getShowMachineBoardButton().setOnAction(event -> {
            Stage machineStage = new Stage();
            NavalView machineView = new NavalView(machineStage);
            renderMachineBoard(machineView.getPlayerBoardView());
            machineStage.show();
        });
    }

    private void setupMainBoardInteraction() {
        GridPane mainBoardView = view.getMainBoardView();
        for (int i = 1; i <= 10; i++) {
            for (int j = 1; j <= 10; j++) {
                Label cell = (Label) getNodeFromGridPane(mainBoardView, j, i);
                if (cell == null) {
                    System.out.printf("Warning: Cell at %d,%d is null.%n", i, j);
                    continue;
                }
                int finalRow = i - 1;
                int finalCol = j - 1;
                cell.setOnMouseClicked(event -> {
                    if (model.isGameStarted()) {
                        String result = model.processShot(model.getMachineBoard(), finalRow, finalCol);
                        updateCellStyle(cell, result);
                        view.getMessageLabel().setText("Disparo: " + result);

                        if ("hundido".equals(result)) {
                            model.incrementSunkShips();
                        }

                        saveGameState(); // Guardar el estado tras cada jugada del jugador
                        checkGameStatus();

                        if ("agua".equals(result)) {
                            machineTurn();
                        }
                    } else {
                        view.getMessageLabel().setText("Presiona 'Jugar' para comenzar.");
                    }
                });
            }
        }
    }

    private void machineTurn() {
        Random random = new Random();
        boolean turnOver = false;

        while (!turnOver) {
            int row = random.nextInt(10);
            int col = random.nextInt(10);
            String result = model.processShot(model.getPlayerBoard(), row, col);

            if (!"repetido".equals(result)) {
                GridPane playerBoardView = view.getPlayerBoardView();
                Label cell = (Label) getNodeFromGridPane(playerBoardView, col + 1, row + 1);
                if (cell == null) {
                    System.out.printf("Warning: Cell at %d,%d is null (machine turn).%n", row, col);
                    continue;
                }
                updateCellStyle(cell, result);
                view.getMessageLabel().setText("Turno de la máquina: " + result);
                saveGameState(); // Guardar el estado tras cada jugada de la máquina
                checkGameStatus();
                turnOver = "agua".equals(result);
            }
        }
    }

    private void checkGameStatus() {
        if (isFleetSunk(model.getMachineBoard())) {
            view.getMessageLabel().setText("¡Felicidades! Has hundido toda la flota enemiga. ¡Ganaste!");
            disableBoard(view.getMainBoardView());
        } else if (isFleetSunk(model.getPlayerBoard())) {
            view.getMessageLabel().setText("Lo siento, la máquina ha hundido toda tu flota. ¡Perdiste!");
            disableBoard(view.getMainBoardView());
        }
    }

    private boolean isFleetSunk(List<List<String>> board) {
        for (List<String> row : board) {
            if (row.contains("barco")) {
                return false;
            }
        }
        return true;
    }

    private void disableBoard(GridPane board) {
        board.setDisable(true);
    }

    private void saveGameState() {
        try {
            model.saveGame("gameState.ser");
            model.savePlayerStats("playerStats.txt");
        } catch (IOException e) {
            view.getMessageLabel().setText("Error al guardar el estado del juego.");
        }
    }

    private void loadGameState() throws IOException, ClassNotFoundException {
        model = NavalModel.loadGame("gameState.ser");
        model.loadPlayerStats("playerStats.txt");

        if (isGameFinished()) {
            view.getMessageLabel().setText("El juego guardado ya terminó. Iniciando nuevo juego.");
            model.startGame();
        } else {
            renderPlayerBoard();
            view.getMessageLabel().setText("Juego cargado exitosamente. ¡Continúa jugando!");
        }
    }

    private boolean isGameFinished() {
        return isFleetSunk(model.getPlayerBoard()) || isFleetSunk(model.getMachineBoard());
    }

    private void renderPlayerBoard() {
        GridPane playerBoardView = view.getPlayerBoardView();
        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 10; col++) {
                if ("barco".equals(model.getPlayerBoard().get(row).get(col))) {
                    Label cell = (Label) getNodeFromGridPane(playerBoardView, col + 1, row + 1);
                    cell.setStyle("-fx-background-color: gray; -fx-border-color: black;");
                }
            }
        }
    }

    private void renderMachineBoard(GridPane machineBoardView) {
        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 10; col++) {
                if ("barco".equals(model.getMachineBoard().get(row).get(col))) {
                    Label cell = (Label) getNodeFromGridPane(machineBoardView, col + 1, row + 1);
                    if (cell != null) {
                        cell.setStyle("-fx-background-color: gray; -fx-border-color: black;");
                    }
                }
            }
        }
    }

    private void updateCellStyle(Label cell, String result) {
        switch (result) {
            case "agua":
                cell.setStyle("-fx-background-image: url('/com/example/navalbattle/cruz.png'); -fx-background-size: cover;");
                break;
            case "tocado":
                cell.setStyle("-fx-background-image: url('/com/example/navalbattle/bomba.png'); -fx-background-size: cover;");
                break;
            case "hundido":
                cell.setStyle("-fx-background-image: url('/com/example/navalbattle/fuego.png'); -fx-background-size: cover;");
                break;
        }
    }

    private Object getNodeFromGridPane(GridPane gridPane, int col, int row) {
        for (javafx.scene.Node node : gridPane.getChildren()) {
            Integer colIndex = GridPane.getColumnIndex(node);
            Integer rowIndex = GridPane.getRowIndex(node);
            if (colIndex != null && rowIndex != null && colIndex == col && rowIndex == row) {
                return node;
            }
        }
        return null;
    }
}
