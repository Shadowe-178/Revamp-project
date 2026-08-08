package com.example.smartchess.dto;

public class BoardStateDto {
    private String[][] board;
    private String currentPlayer;
    private String message;
    private boolean check;
    private boolean gameOver;
    private String winner;

    public String[][] getBoard() {
        return board;
    }

    public void setBoard(String[][] board) {
        this.board = board;
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

    public boolean isCheck() { return check; }
    public void setCheck(boolean check) { this.check = check; }
    public boolean isGameOver() { return gameOver; }
    public void setGameOver(boolean gameOver) { this.gameOver = gameOver; }
    public String getWinner() { return winner; }
    public void setWinner(String winner) { this.winner = winner; }
}
