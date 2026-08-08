package com.example.smartchess.service.chess;

import com.example.smartchess.dto.ChessMoveDto;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service
public class ChessAIService {

    private final ChessRuleService ruleService;
    private final Random random = new Random();

    public ChessAIService(ChessRuleService ruleService) {
        this.ruleService = ruleService;
    }

    public ChessMoveDto randomMove(String[][] board, String color) {
        List<ChessMoveDto> moves = ruleService.findLegalMoves(board, color);
        if (moves.isEmpty()) {
            return null;
        }
        return moves.get(random.nextInt(moves.size()));
    }

    public ChessMoveDto minimaxMove(String[][] board, String color) {
        List<ChessMoveDto> moves = ruleService.findLegalMoves(board, color);
        if (moves.isEmpty()) {
            return null;
        }

        ChessMoveDto bestMove = null;
        int bestScore = Integer.MIN_VALUE;
        for (ChessMoveDto move : moves) {
            String[][] nextBoard = applyMove(board, move);
            int score = minimax(nextBoard, oppositeColor(color), 2, false, color);
            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
            }
        }
        if (bestMove != null) {
            bestMove.setScore(bestScore);
        }
        return bestMove;
    }

    public ChessMoveDto alphaBetaMove(String[][] board, String color) {
        List<ChessMoveDto> moves = ruleService.findLegalMoves(board, color);
        if (moves.isEmpty()) {
            return null;
        }

        ChessMoveDto bestMove = null;
        int bestScore = Integer.MIN_VALUE;
        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;
        for (ChessMoveDto move : moves) {
            String[][] nextBoard = applyMove(board, move);
            int score = alphaBeta(nextBoard, oppositeColor(color), 2, false, color, alpha, beta);
            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
            }
            alpha = Math.max(alpha, score);
        }
        if (bestMove != null) {
            bestMove.setScore(bestScore);
        }
        return bestMove;
    }

    private int minimax(String[][] board, String color, int depth, boolean maximizing, String rootColor) {
        if (depth == 0) {
            return evaluateBoard(board, rootColor);
        }
        List<ChessMoveDto> moves = ruleService.findLegalMoves(board, color);
        if (moves.isEmpty()) {
            if (ruleService.isInCheck(board, color)) {
                return maximizing ? Integer.MIN_VALUE + 1 : Integer.MAX_VALUE - 1;
            }
            return 0;
        }

        int best = maximizing ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        for (ChessMoveDto move : moves) {
            String[][] nextBoard = applyMove(board, move);
            int score = minimax(nextBoard, oppositeColor(color), depth - 1, !maximizing, rootColor);
            if (maximizing) {
                best = Math.max(best, score);
            } else {
                best = Math.min(best, score);
            }
        }
        return best;
    }

    private int alphaBeta(String[][] board, String color, int depth, boolean maximizing, String rootColor, int alpha, int beta) {
        if (depth == 0) {
            return evaluateBoard(board, rootColor);
        }
        List<ChessMoveDto> moves = ruleService.findLegalMoves(board, color);
        if (moves.isEmpty()) {
            if (ruleService.isInCheck(board, color)) {
                return maximizing ? Integer.MIN_VALUE + 1 : Integer.MAX_VALUE - 1;
            }
            return 0;
        }

        if (maximizing) {
            int value = Integer.MIN_VALUE;
            for (ChessMoveDto move : moves) {
                String[][] nextBoard = applyMove(board, move);
                value = Math.max(value, alphaBeta(nextBoard, oppositeColor(color), depth - 1, false, rootColor, alpha, beta));
                alpha = Math.max(alpha, value);
                if (alpha >= beta) {
                    break;
                }
            }
            return value;
        } else {
            int value = Integer.MAX_VALUE;
            for (ChessMoveDto move : moves) {
                String[][] nextBoard = applyMove(board, move);
                value = Math.min(value, alphaBeta(nextBoard, oppositeColor(color), depth - 1, true, rootColor, alpha, beta));
                beta = Math.min(beta, value);
                if (beta <= alpha) {
                    break;
                }
            }
            return value;
        }
    }

    private int evaluateBoard(String[][] board, String color) {
        int score = 0;
        int redValue = 0;
        int blackValue = 0;
        for (String[] row : board) {
            for (String piece : row) {
                if (piece == null || piece.isEmpty()) {
                    continue;
                }
                int value = getPieceValue(piece);
                if (piece.startsWith("r")) {
                    redValue += value;
                } else {
                    blackValue += value;
                }
            }
        }
        if (color.equalsIgnoreCase("red")) {
            score = redValue - blackValue;
            if (ruleService.isInCheck(board, "red")) {
                score -= 800;
            }
            if (ruleService.isCheckmate(board, "red")) {
                score -= 12000;
            }
            if (ruleService.isInCheck(board, "black")) {
                score += 400;
            }
            if (ruleService.isCheckmate(board, "black")) {
                score += 12000;
            }
        } else {
            score = blackValue - redValue;
            if (ruleService.isInCheck(board, "black")) {
                score -= 800;
            }
            if (ruleService.isCheckmate(board, "black")) {
                score -= 12000;
            }
            if (ruleService.isInCheck(board, "red")) {
                score += 400;
            }
            if (ruleService.isCheckmate(board, "red")) {
                score += 12000;
            }
        }
        return score;
    }

    private int getPieceValue(String piece) {
        switch (piece.substring(1)) {
            case "K":
                return 10000;
            case "R":
                return 900;
            case "C":
                return 450;
            case "N":
                return 350;
            case "E":
                return 200;
            case "A":
                return 200;
            case "P":
                return 100;
            default:
                return 0;
        }
    }

    private String oppositeColor(String color) {
        return "red".equalsIgnoreCase(color) ? "black" : "red";
    }

    private String[][] applyMove(String[][] board, ChessMoveDto move) {
        String[][] next = copyBoard(board);
        int[] from = parsePosition(move.getFrom());
        int[] to = parsePosition(move.getTo());
        next[to[1]][to[0]] = next[from[1]][from[0]];
        next[from[1]][from[0]] = "";
        return next;
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
