package com.example.smartchess.dto;

public class ChessMoveDto {
    private String from;
    private String to;
    private String piece;
    private String capturedPiece;
    private int score;

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getPiece() {
        return piece;
    }

    public void setPiece(String piece) {
        this.piece = piece;
    }

    public String getCapturedPiece() {
        return capturedPiece;
    }

    public void setCapturedPiece(String capturedPiece) {
        this.capturedPiece = capturedPiece;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
}
