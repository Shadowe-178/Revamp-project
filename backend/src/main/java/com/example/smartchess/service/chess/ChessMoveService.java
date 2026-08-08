package com.example.smartchess.service.chess;

import org.springframework.stereotype.Service;

@Service
public class ChessMoveService {

    private final ChessRuleService ruleService;

    public ChessMoveService(ChessRuleService ruleService) {
        this.ruleService = ruleService;
    }

    public String[][] applyMove(String[][] board, String from, String to) {
        if (!ruleService.isValidMove(board, from, to)) {
            throw new IllegalArgumentException("不合法的走法");
        }
        String[][] newBoard = copyBoard(board);
        int[] source = parsePosition(from);
        int[] target = parsePosition(to);
        newBoard[target[1]][target[0]] = newBoard[source[1]][source[0]];
        newBoard[source[1]][source[0]] = "";
        return newBoard;
    }

    public String[][] applyMoveForColor(String[][] board, String from, String to, String color) {
        int[] source = parsePosition(from);
        String piece = board[source[1]][source[0]];
        String prefix = "red".equalsIgnoreCase(color) ? "r" : "b";
        if (piece == null || !piece.startsWith(prefix)) {
            throw new IllegalArgumentException("It is not this side's turn");
        }
        return applyMove(board, from, to);
    }

    private String[][] copyBoard(String[][] board) {
        String[][] copy = new String[board.length][];
        for (int i = 0; i < board.length; i++) {
            copy[i] = board[i].clone();
        }
        return copy;
    }

    private int[] parsePosition(String pos) {
        String[] parts = pos.split(",");
        return new int[]{Integer.parseInt(parts[0]), Integer.parseInt(parts[1])};
    }
}
