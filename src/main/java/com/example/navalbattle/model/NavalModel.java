package com.example.navalbattle.model;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Modelo que representa el estado del juego Batalla Naval.
 */
public class NavalModel implements Serializable {
    private final List<List<String>> playerBoard;
    private final List<List<String>> machineBoard;
    private boolean gameStarted;
    private String playerName;
    private int sunkShips;

    public NavalModel() {
        playerBoard = new ArrayList<>();
        machineBoard = new ArrayList<>();
        initializeBoards();
        generateFleet(playerBoard);
        generateFleet(machineBoard);
        gameStarted = false;
        sunkShips = 0;
    }

    private void initializeBoards() {
        for (int i = 0; i < 10; i++) {
            List<String> playerRow = new ArrayList<>();
            List<String> machineRow = new ArrayList<>();
            for (int j = 0; j < 10; j++) {
                playerRow.add("agua");
                machineRow.add("agua");
            }
            playerBoard.add(playerRow);
            machineBoard.add(machineRow);
        }
    }

    private void generateFleet(List<List<String>> board) {
        int[] shipSizes = {4, 3, 3, 2, 2, 2, 1, 1, 1, 1};
        Random random = new Random();

        for (int size : shipSizes) {
            boolean placed = false;
            while (!placed) {
                int row = random.nextInt(10);
                int col = random.nextInt(10);
                boolean horizontal = random.nextBoolean();

                if (canPlaceShip(board, row, col, size, horizontal)) {
                    placeShip(board, row, col, size, horizontal);
                    placed = true;
                }
            }
        }
    }

    private boolean canPlaceShip(List<List<String>> board, int row, int col, int size, boolean horizontal) {
        if (horizontal) {
            if (col + size > 10) return false;
            for (int i = 0; i < size; i++) {
                if (!"agua".equals(board.get(row).get(col + i))) return false;
            }
        } else {
            if (row + size > 10) return false;
            for (int i = 0; i < size; i++) {
                if (!"agua".equals(board.get(row + i).get(col))) return false;
            }
        }
        return true;
    }

    private void placeShip(List<List<String>> board, int row, int col, int size, boolean horizontal) {
        for (int i = 0; i < size; i++) {
            if (horizontal) {
                board.get(row).set(col + i, "barco");
            } else {
                board.get(row + i).set(col, "barco");
            }
        }
    }

    public String processShot(List<List<String>> board, int row, int col) {
        String cell = board.get(row).get(col);
        if ("agua".equals(cell)) {
            board.get(row).set(col, "agua disparada");
            return "agua";
        } else if ("barco".equals(cell)) {
            board.get(row).set(col, "tocado");
            return isShipSunk(board, row, col) ? "hundido" : "tocado";
        }
        return "repetido";
    }

    private boolean isShipSunk(List<List<String>> board, int row, int col) {
        // Placeholder for actual logic
        return true;
    }

    // Persistencia
    public void saveGame(String filePath) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(this);
        }
    }

    public static NavalModel loadGame(String filePath) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            return (NavalModel) ois.readObject();
        }
    }

    public void savePlayerStats(String filePath) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write("Nickname: " + playerName + "\n");
            writer.write("Barcos hundidos: " + sunkShips + "\n");
        }
    }

    public void loadPlayerStats(String filePath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            playerName = reader.readLine().split(": ")[1];
            sunkShips = Integer.parseInt(reader.readLine().split(": ")[1]);
        }
    }

    public List<List<String>> getPlayerBoard() {
        return playerBoard;
    }

    public List<List<String>> getMachineBoard() {
        return machineBoard;
    }

    public boolean isGameStarted() {
        return gameStarted;
    }

    public void startGame() {
        this.gameStarted = true;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public int getSunkShips() {
        return sunkShips;
    }

    public void incrementSunkShips() {
        this.sunkShips++;
    }
}
