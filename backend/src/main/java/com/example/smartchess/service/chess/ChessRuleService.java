package com.example.smartchess.service.chess;

import com.example.smartchess.dto.ChessMoveDto;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ChessRuleService {

    public String[][] initializeBoard() {
        String[][] board = new String[10][9];
        board[0] = new String[]{"bR", "bN", "bE", "bA", "bK", "bA", "bE", "bN", "bR"};
        board[1] = new String[]{"", "", "", "", "", "", "", "", ""};
        board[2] = new String[]{"", "bC", "", "", "", "", "", "bC", ""};
        board[3] = new String[]{"bP", "", "bP", "", "bP", "", "bP", "", "bP"};
        board[4] = new String[]{"", "", "", "", "", "", "", "", ""};
        board[5] = new String[]{"", "", "", "", "", "", "", "", ""};
        board[6] = new String[]{"rP", "", "rP", "", "rP", "", "rP", "", "rP"};
        board[7] = new String[]{"", "rC", "", "", "", "", "", "rC", ""};
        board[8] = new String[]{"", "", "", "", "", "", "", "", ""};
        board[9] = new String[]{"rR", "rN", "rE", "rA", "rK", "rA", "rE", "rN", "rR"};
        return board;
    }

    public boolean isValidMove(String[][] board, String from, String to) {
        int[] source = parsePosition(from);
        int[] target = parsePosition(to);
        if (source == null || target == null || !inBounds(source[0], source[1]) || !inBounds(target[0], target[1])
                || !isPseudoLegalMove(board, from, to)) {
            return false;
        }
        String[][] afterMove = copyBoard(board);
        afterMove[target[1]][target[0]] = afterMove[source[1]][source[0]];
        afterMove[source[1]][source[0]] = "";
        return !isInCheck(afterMove, board[source[1]][source[0]].startsWith("r") ? "red" : "black");
    }

    private boolean isPseudoLegalMove(String[][] board, String from, String to) {
        int[] source = parsePosition(from);
        int[] target = parsePosition(to);
        if (source == null || target == null) {
            return false;
        }
        int sx = source[0];
        int sy = source[1];
        int tx = target[0];
        int ty = target[1];
        if (!inBounds(sx, sy) || !inBounds(tx, ty)) {
            return false;
        }

        String piece = board[sy][sx];
        if (piece == null || piece.isEmpty()) {
            return false;
        }
        String targetPiece = board[ty][tx];
        if (targetPiece != null && !targetPiece.isEmpty() && isSameColor(piece, targetPiece)) {
            return false;
        }
        String type = piece.substring(1);
        boolean capture = targetPiece != null && !targetPiece.isEmpty();

        switch (type) {
            case "R":
                return isStraightMove(sx, sy, tx, ty) && isPathClear(board, sx, sy, tx, ty);
            case "C":
                if (!isStraightMove(sx, sy, tx, ty)) {
                    return false;
                }
                int blockers = countPiecesBetween(board, sx, sy, tx, ty);
                return capture ? blockers == 1 : blockers == 0;
            case "N":
                return isValidHorseMove(board, sx, sy, tx, ty);
            case "E":
                return isValidElephantMove(board, sx, sy, tx, ty, piece);
            case "A":
                return isValidAdvisorMove(tx, ty, piece) && dx(sx, tx) == 1 && dy(sy, ty) == 1;
            case "K":
                return isValidKingMove(tx, ty, piece) && dx(sx, tx) + dy(sy, ty) == 1;
            case "P":
                return isValidPawnMove(board, sx, sy, tx, ty, piece);
            default:
                return false;
        }
    }

    public List<ChessMoveDto> findLegalMoves(String[][] board, String color) {
        List<ChessMoveDto> legalMoves = new ArrayList<>();
        for (int y = 0; y < board.length; y++) {
            for (int x = 0; x < board[y].length; x++) {
                String piece = board[y][x];
                if (piece == null || piece.isEmpty() || !piece.startsWith(color.substring(0, 1).toLowerCase())) {
                    continue;
                }
                for (int ty = 0; ty < board.length; ty++) {
                    for (int tx = 0; tx < board[ty].length; tx++) {
                        String to = tx + "," + ty;
                        String from = x + "," + y;
                        if (isValidMove(board, from, to)) {
                            ChessMoveDto move = new ChessMoveDto();
                            move.setFrom(from);
                            move.setTo(to);
                            move.setPiece(piece);
                            move.setCapturedPiece(board[ty][tx]);
                            legalMoves.add(move);
                        }
                    }
                }
            }
        }
        return legalMoves;
    }

    public boolean isInCheck(String[][] board, String color) {
        String king = color != null && color.equalsIgnoreCase("red") ? "rK" : "bK";
        int kingX = -1;
        int kingY = -1;
        for (int y = 0; y < board.length; y++) {
            for (int x = 0; x < board[y].length; x++) {
                if (king.equals(board[y][x])) {
                    kingX = x;
                    kingY = y;
                    break;
                }
            }
        }
        if (kingX < 0) {
            return true;
        }
        String opponentPrefix = king.startsWith("r") ? "b" : "r";
        for (int y = 0; y < board.length; y++) {
            for (int x = 0; x < board[y].length; x++) {
                String piece = board[y][x];
                if (piece != null && piece.startsWith(opponentPrefix)
                        && isPseudoLegalMove(board, x + "," + y, kingX + "," + kingY)) {
                    return true;
                }
            }
        }
        return kingsFacing(board);
    }

    public boolean isCheckmate(String[][] board, String color) {
        return isInCheck(board, color) && findLegalMoves(board, color).isEmpty();
    }

    public boolean hasKing(String[][] board, String color) {
        String king = "red".equalsIgnoreCase(color) ? "rK" : "bK";
        for (String[] row : board) {
            for (String piece : row) {
                if (king.equals(piece)) return true;
            }
        }
        return false;
    }

    private boolean kingsFacing(String[][] board) {
        int redY = -1;
        int blackY = -1;
        int file = -1;
        for (int y = 0; y < board.length; y++) {
            for (int x = 0; x < board[y].length; x++) {
                if ("rK".equals(board[y][x])) {
                    redY = y;
                    file = x;
                }
                if ("bK".equals(board[y][x]) && (file == -1 || file == x)) {
                    blackY = y;
                    file = x;
                }
            }
        }
        if (redY < 0 || blackY < 0 || file < 0 || !"rK".equals(board[redY][file]) || !"bK".equals(board[blackY][file])) {
            return false;
        }
        for (int y = Math.min(redY, blackY) + 1; y < Math.max(redY, blackY); y++) {
            if (board[y][file] != null && !board[y][file].isEmpty()) return false;
        }
        return true;
    }

    private String[][] copyBoard(String[][] board) {
        String[][] copy = new String[board.length][];
        for (int i = 0; i < board.length; i++) copy[i] = board[i].clone();
        return copy;
    }

    private int[] parsePosition(String pos) {
        if (pos == null || !pos.contains(",")) {
            return null;
        }
        String[] parts = pos.split(",");
        if (parts.length != 2) {
            return null;
        }
        try {
            return new int[]{Integer.parseInt(parts[0]), Integer.parseInt(parts[1])};
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private boolean inBounds(int x, int y) {
        return x >= 0 && x < 9 && y >= 0 && y < 10;
    }

    private boolean isSameColor(String a, String b) {
        return a.charAt(0) == b.charAt(0);
    }

    private boolean isStraightMove(int sx, int sy, int tx, int ty) {
        return sx == tx || sy == ty;
    }

    private int dx(int sx, int tx) {
        return Math.abs(tx - sx);
    }

    private int dy(int sy, int ty) {
        return Math.abs(ty - sy);
    }

    private boolean isPathClear(String[][] board, int sx, int sy, int tx, int ty) {
        int dx = Integer.compare(tx, sx);
        int dy = Integer.compare(ty, sy);
        int x = sx + dx;
        int y = sy + dy;
        while (x != tx || y != ty) {
            if (board[y][x] != null && !board[y][x].isEmpty()) {
                return false;
            }
            x += dx;
            y += dy;
        }
        return true;
    }

    private int countPiecesBetween(String[][] board, int sx, int sy, int tx, int ty) {
        int deltaX = Integer.compare(tx, sx);
        int deltaY = Integer.compare(ty, sy);
        int count = 0;
        int x = sx + deltaX;
        int y = sy + deltaY;
        while (x != tx || y != ty) {
            if (board[y][x] != null && !board[y][x].isEmpty()) {
                count++;
            }
            x += deltaX;
            y += deltaY;
        }
        return count;
    }

    private boolean isValidHorseMove(String[][] board, int sx, int sy, int tx, int ty) {
        int dx = dx(sx, tx);
        int dy = dy(sy, ty);
        if (!((dx == 1 && dy == 2) || (dx == 2 && dy == 1))) {
            return false;
        }
        int legX = sx + (tx - sx == 0 ? 0 : (tx - sx) / Math.abs(tx - sx));
        int legY = sy + (ty - sy == 0 ? 0 : (ty - sy) / Math.abs(ty - sy));
        if (dx == 2) {
            legX = sx + (tx - sx) / 2;
            legY = sy;
        } else {
            legX = sx;
            legY = sy + (ty - sy) / 2;
        }
        return board[legY][legX] == null || board[legY][legX].isEmpty();
    }

    private boolean isValidElephantMove(String[][] board, int sx, int sy, int tx, int ty, String piece) {
        if (dx(sx, tx) != 2 || dy(sy, ty) != 2) {
            return false;
        }
        if (piece.startsWith("r") && ty < 5) {
            return false;
        }
        if (piece.startsWith("b") && ty > 4) {
            return false;
        }
        int eyeX = (sx + tx) / 2;
        int eyeY = (sy + ty) / 2;
        return board[eyeY][eyeX] == null || board[eyeY][eyeX].isEmpty();
    }

    private boolean isValidAdvisorMove(int tx, int ty, String piece) {
        if (dx(0, tx) != 0) {
            // no-op
        }
        if (piece.startsWith("r")) {
            return tx >= 3 && tx <= 5 && ty >= 7 && ty <= 9;
        }
        return tx >= 3 && tx <= 5 && ty >= 0 && ty <= 2;
    }

    private boolean isValidKingMove(int tx, int ty, String piece) {
        if (piece.startsWith("r")) {
            return tx >= 3 && tx <= 5 && ty >= 7 && ty <= 9;
        }
        return tx >= 3 && tx <= 5 && ty >= 0 && ty <= 2;
    }

    private boolean isValidPawnMove(String[][] board, int sx, int sy, int tx, int ty, String piece) {
        int dx = dx(sx, tx);
        int dy = ty - sy;
        boolean red = piece.startsWith("r");
        int forward = red ? -1 : 1;
        if (dx == 0 && dy == forward) {
            return true;
        }
        boolean crossedRiver = red ? sy <= 4 : sy >= 5;
        if (crossedRiver && dx == 1 && dy == 0) {
            return true;
        }
        return false;
    }
}
