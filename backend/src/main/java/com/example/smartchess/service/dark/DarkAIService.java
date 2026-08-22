package com.example.smartchess.service.dark;

import com.example.smartchess.dto.ChessMoveDto;
import com.example.smartchess.dto.DarkBoardStateDto;
import com.example.smartchess.dto.DarkFlipDto;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class DarkAIService {

    private final DarkRuleService ruleService;
    private final Random random = new Random();

    public DarkAIService(DarkRuleService ruleService) {
        this.ruleService = ruleService;
    }

    public ChessMoveDto randomMove(String[][] pieces, boolean[][] revealed, String color) {
        List<ChessMoveDto> moves = ruleService.findLegalMoves(pieces, revealed, color);

        if (moves.isEmpty()) {
            return null;
        }

        return moves.get(random.nextInt(moves.size()));
    }

    public ChessMoveDto darkMinimaxMove(String[][] pieces, boolean[][] revealed, String color) {

        List<ChessMoveDto> moves =
                ruleService.findLegalMoves(pieces, revealed, color);

        if (moves.isEmpty()) {
            return null;
        }

        ChessMoveDto bestMove = null;
        int bestScore = Integer.MIN_VALUE;

        for (ChessMoveDto move : moves) {

            String[][] nextPieces = applyMove(pieces, move);

            int score =
                    minimax(
                            nextPieces,
                            revealed,
                            oppositeColor(color),
                            2,
                            false,
                            color);

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

    private int minimax(
            String[][] pieces,
            boolean[][] revealed,
            String color,
            int depth,
            boolean maximizing,
            String rootColor) {

        if (depth == 0) {
            return evaluateBoard(pieces, revealed, rootColor);
        }

        List<ChessMoveDto> moves =
                ruleService.findLegalMoves(pieces, revealed, color);

        if (moves.isEmpty()) {
            return 0;
        }

        int best =
                maximizing
                        ? Integer.MIN_VALUE
                        : Integer.MAX_VALUE;

        for (ChessMoveDto move : moves) {

            String[][] nextPieces = applyMove(pieces, move);

            int score =
                    minimax(
                            nextPieces,
                            revealed,
                            oppositeColor(color),
                            depth - 1,
                            !maximizing,
                            rootColor);

            if (maximizing) {
                best = Math.max(best, score);
            } else {
                best = Math.min(best, score);
            }
        }

        return best;
    }

    private int evaluateBoard(
            String[][] pieces,
            boolean[][] revealed,
            String color) {

        int score = 0;

        for (int y = 0; y < pieces.length; y++) {

            for (int x = 0; x < pieces[y].length; x++) {

                String piece = pieces[y][x];

                if (piece == null || piece.isEmpty()) {
                    continue;
                }

                int value = getRankValue(piece);

                if (piece.startsWith(color.substring(0, 1).toLowerCase())) {
                    score += value;
                } else {
                    score -= value;
                }

                if (revealed[y][x]) {
                    score += piece.startsWith(color.substring(0, 1).toLowerCase())
                            ? 10
                            : -10;
                }
            }
        }

        return score;
    }

    private String[][] applyMove(
            String[][] pieces,
            ChessMoveDto move) {

        int[] from = parsePosition(move.getFrom());
        int[] to = parsePosition(move.getTo());

        return ruleService.applyMove(
                pieces,
                from[0],
                from[1],
                to[0],
                to[1]);
    }

    private int[] parsePosition(String pos) {

        String[] parts = pos.split(",");

        return new int[]{
                Integer.parseInt(parts[0]),
                Integer.parseInt(parts[1])
        };
    }

    private int getRankValue(String piece) {

        switch (piece.substring(1)) {

            case "K":
                return 700;

            case "A":
                return 600;

            case "E":
                return 500;

            case "R":
                return 400;

            case "N":
                return 300;

            case "C":
                return 200;

            case "P":
                return 100;

            default:
                return 0;
        }
    }

    public DarkFlipDto findBestFlip(boolean[][] revealed) {

        List<int[]> hidden = new ArrayList<>();

        for (int y = 0; y < revealed.length; y++) {

            for (int x = 0; x < revealed[y].length; x++) {

                if (!revealed[y][x]) {
                    hidden.add(new int[]{x, y});
                }
            }
        }

        if (hidden.isEmpty()) {
            return null;
        }

        int[] pick =
                hidden.get(random.nextInt(hidden.size()));

        return new DarkFlipDto(pick[0], pick[1]);
    }

    private String oppositeColor(String color) {
        return "red".equalsIgnoreCase(color)
                ? "black"
                : "red";
    }

    public DarkFlipDto chooseFlip(boolean[][] revealed) {

        for (int y = 0; y < revealed.length; y++) {

            for (int x = 0; x < revealed[y].length; x++) {

                if (!revealed[y][x]) {

                    DarkFlipDto dto = new DarkFlipDto();
                    dto.setX(x);
                    dto.setY(y);

                    return dto;
                }
            }
        }

        return null;
    }

    // ===========================
    // AI 自動翻牌
    // ===========================

    public DarkBoardStateDto aiFlip(
            String[][] pieces,
            boolean[][] revealed,
            String currentPlayer) {

        DarkFlipDto flip = chooseFlip(revealed);

        if (flip == null) {
            return null;
        }

        boolean[][] nextRevealed =
                ruleService.flipPiece(
                        revealed,
                        flip.getX(),
                        flip.getY());

        DarkBoardStateDto state = new DarkBoardStateDto();
        state.setPieces(pieces);
        state.setRevealed(nextRevealed);
        state.setCurrentPlayer(oppositeColor(currentPlayer));
        state.setMessage("AI 翻牌");

        return state;
    }
}