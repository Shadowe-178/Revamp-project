package com.example.smartchess.dto;

public class DarkBoardStateDto {
    private String[][] pieces;
    private boolean[][] revealed;
    private String currentPlayer;
    private String message;
    private boolean gameOver;
    private String winner;

    public String[][] getPieces() {
        return pieces;
    }

    public void setPieces(String[][] pieces) {
        this.pieces = pieces;
    }

    public boolean[][] getRevealed() {
        return revealed;
    }

    public void setRevealed(boolean[][] revealed) {
        this.revealed = revealed;
    }

    public String getCurrentPlayer() {
        return currentPlayer;
    }

    public void setCurrentPlayer(String currentPlayer) {
        this.currentPlayer = currentPlayer;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }

    public String getWinner() {
        return winner;
    }

    public void setWinner(String winner) {
        this.winner = winner;
    }
}