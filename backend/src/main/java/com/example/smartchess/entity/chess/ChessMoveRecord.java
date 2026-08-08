package com.example.smartchess.entity.chess;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "chess_move_record")
public class ChessMoveRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long matchId;
    private String fromPosition;
    private String toPosition;
    private String piece;
    private String capturedPiece;
    private long moveTimeMillis;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMatchId() {
        return matchId;
    }

    public void setMatchId(Long matchId) {
        this.matchId = matchId;
    }

    public String getFromPosition() {
        return fromPosition;
    }

    public void setFromPosition(String fromPosition) {
        this.fromPosition = fromPosition;
    }

    public String getToPosition() {
        return toPosition;
    }

    public void setToPosition(String toPosition) {
        this.toPosition = toPosition;
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

    public long getMoveTimeMillis() {
        return moveTimeMillis;
    }

    public void setMoveTimeMillis(long moveTimeMillis) {
        this.moveTimeMillis = moveTimeMillis;
    }
}
