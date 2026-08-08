package com.example.smartchess.dto;

public class PlayerProfileDto {
    private String username;
    private String displayName;
    private int totalGames;
    private int winCount;
    private int chessGames;
    private int darkGames;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public int getTotalGames() {
        return totalGames;
    }

    public void setTotalGames(int totalGames) {
        this.totalGames = totalGames;
    }

    public int getWinCount() {
        return winCount;
    }

    public void setWinCount(int winCount) {
        this.winCount = winCount;
    }

    public int getChessGames() {
        return chessGames;
    }

    public void setChessGames(int chessGames) {
        this.chessGames = chessGames;
    }

    public int getDarkGames() {
        return darkGames;
    }

    public void setDarkGames(int darkGames) {
        this.darkGames = darkGames;
    }

    public double getWinRate() {
        return totalGames == 0 ? 0 : (double) winCount / totalGames * 100.0;
    }
}
