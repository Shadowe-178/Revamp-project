package com.example.smartchess.dto;

public class AiRequestDto {
    private String[][] board;
    private boolean[][] revealed;
    private String color;
    private String level;

    public String[][] getBoard() {
        return board;
    }

    public void setBoard(String[][] board) {
        this.board = board;
    }

    public boolean[][] getRevealed() {
        return revealed;
    }

    public void setRevealed(boolean[][] revealed) {
        this.revealed = revealed;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }
}
