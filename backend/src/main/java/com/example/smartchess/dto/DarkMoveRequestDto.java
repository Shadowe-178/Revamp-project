package com.example.smartchess.dto;

public class DarkMoveRequestDto {
    private String[][] pieces;
    private boolean[][] revealed;
    private String currentPlayer;
    private int fromX;
    private int fromY;
    private int toX;
    private int toY;

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

    public String getCurrentPlayer() { return currentPlayer; }
    public void setCurrentPlayer(String currentPlayer) { this.currentPlayer = currentPlayer; }

    public int getFromX() {
        return fromX;
    }

    public void setFromX(int fromX) {
        this.fromX = fromX;
    }

    public int getFromY() {
        return fromY;
    }

    public void setFromY(int fromY) {
        this.fromY = fromY;
    }

    public int getToX() {
        return toX;
    }

    public void setToX(int toX) {
        this.toX = toX;
    }

    public int getToY() {
        return toY;
    }

    public void setToY(int toY) {
        this.toY = toY;
    }
}
