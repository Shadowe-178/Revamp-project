package com.example.smartchess.service.dark;

import com.example.smartchess.dto.ChessMoveDto;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DarkRuleService {

    private static final Map<String, Integer> RANKS = new HashMap<>();

    static {
        RANKS.put("K", 7);
        RANKS.put("A", 6);
        RANKS.put("E", 5);
        RANKS.put("R", 4);
        RANKS.put("N", 3);
        RANKS.put("C", 2);
        RANKS.put("P", 1);
    }

    public String[][] initializeBoard() {
        List<String> pieces = new ArrayList<>();
        pieces.addAll(createSidePieces('r'));
        pieces.addAll(createSidePieces('b'));
        Collections.shuffle(pieces);
        String[][] board = new String[8][4];
        int index = 0;
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 4; x++) {
                board[y][x] = pieces.get(index++);
            }
        }
        return board;
    }

    public boolean isLegalFlip(boolean[][] revealed, int x, int y) {
        return inBounds(x, y) && !revealed[y][x];
    }

    public boolean isLegalMove(String[][] pieces, boolean[][] revealed, int fromX, int fromY, int toX, int toY) {
        if (!inBounds(fromX, fromY) || !inBounds(toX, toY)) {
            return false;
        }
        if (!revealed[fromY][fromX]) {
            return false;
        }
        if (pieces[fromY][fromX] == null || pieces[fromY][fromX].isEmpty()) {
            return false;
        }
        if (revealed[toY][toX] && pieces[toY][toX] == null) {
            return false;
        }
        if (fromX == toX && fromY == toY) {
            return false;
        }
        String attacker = pieces[fromY][fromX];
        String target = pieces[toY][toX];
        boolean capture = target != null && !target.isEmpty();
        if (attacker.charAt(1) == 'C' && capture) {
            return revealed[toY][toX] && !isSameSide(attacker, target)
                    && canCaptureWithCannon(pieces, fromX, fromY, toX, toY);
        }
        if (Math.abs(fromX - toX) + Math.abs(fromY - toY) != 1) return false;
        if (pieces[toY][toX] == null || pieces[toY][toX].isEmpty()) {
            return true;
        }
        if (!revealed[toY][toX]) {
            return false;
        }
        if (isSameSide(attacker, target)) {
            return false;
        }
        return canCapture(attacker, target, pieces, fromX, fromY, toX, toY);
    }

    public String[][] applyMove(String[][] pieces, int fromX, int fromY, int toX, int toY) {
        String[][] copy = copyBoard(pieces);
        copy[toY][toX] = copy[fromY][fromX];
        copy[fromY][fromX] = "";
        return copy;
    }

    public boolean[][] flipPiece(boolean[][] revealed, int x, int y) {
        boolean[][] copy = copyRevealed(revealed);
        copy[y][x] = true;
        return copy;
    }

    public List<ChessMoveDto> findLegalMoves(String[][] pieces, boolean[][] revealed, String color) {
        List<ChessMoveDto> legalMoves = new ArrayList<>();
        for (int y = 0; y < pieces.length; y++) {
            for (int x = 0; x < pieces[y].length; x++) {
                if (!revealed[y][x]) {
                    continue;
                }
                String piece = pieces[y][x];
                if (piece == null || piece.isEmpty() || !piece.startsWith(color.substring(0, 1).toLowerCase())) {
                    continue;
                }
                for (int ty = 0; ty < pieces.length; ty++) {
                    for (int tx = 0; tx < pieces[ty].length; tx++) {
                    if (isLegalMove(pieces, revealed, x, y, tx, ty)) {
                        ChessMoveDto move = new ChessMoveDto();
                        move.setFrom(x + "," + y);
                        move.setTo(tx + "," + ty);
                        move.setPiece(piece);
                        move.setCapturedPiece(pieces[ty][tx]);
                        legalMoves.add(move);
                    }
                    }
                }
            }
        }
        return legalMoves;
    }

    private List<String> createSidePieces(char color) {
        String prefix = color + "";
        List<String> list = new ArrayList<>();
        list.add(prefix + "K");
        list.add(prefix + "A");
        list.add(prefix + "A");
        list.add(prefix + "E");
        list.add(prefix + "E");
        list.add(prefix + "R");
        list.add(prefix + "R");
        list.add(prefix + "N");
        list.add(prefix + "N");
        list.add(prefix + "C");
        list.add(prefix + "C");
        list.add(prefix + "P");
        list.add(prefix + "P");
        list.add(prefix + "P");
        list.add(prefix + "P");
        list.add(prefix + "P");
        return list;
    }

    private boolean canCapture(String attacker, String target, String[][] pieces, int fromX, int fromY, int toX, int toY) {
        if (attacker.charAt(1) == 'C') {
            return canCaptureWithCannon(pieces, fromX, fromY, toX, toY);
        }
        if (attacker.charAt(1) == 'K' && target.charAt(1) == 'P') {
            return false;
        }
        if (attacker.charAt(1) == 'P' && target.charAt(1) == 'K') {
            return true;
        }
        return getRank(attacker) >= getRank(target);
    }

    private boolean canCaptureWithCannon(String[][] pieces, int fromX, int fromY, int toX, int toY) {
        if (fromX != toX && fromY != toY) {
            return false;
        }
        int dx = Integer.compare(toX, fromX);
        int dy = Integer.compare(toY, fromY);
        int count = 0;
        int x = fromX + dx;
        int y = fromY + dy;
        while (x != toX || y != toY) {
            if (pieces[y][x] != null && !pieces[y][x].isEmpty()) {
                count++;
            }
            x += dx;
            y += dy;
        }
        return count == 1;
    }

    private int getRank(String piece) {
        return RANKS.getOrDefault(piece.substring(1), 0);
    }

    private boolean inBounds(int x, int y) {
        return x >= 0 && x < 4 && y >= 0 && y < 8;
    }

    private boolean isSameSide(String a, String b) {
        return a.charAt(0) == b.charAt(0);
    }

    private String[][] copyBoard(String[][] board) {
        String[][] result = new String[board.length][];
        for (int i = 0; i < board.length; i++) {
            result[i] = board[i].clone();
        }
        return result;
    }

    private boolean[][] copyRevealed(boolean[][] revealed) {
        boolean[][] copy = new boolean[revealed.length][];
        for (int i = 0; i < revealed.length; i++) {
            copy[i] = revealed[i].clone();
        }
        return copy;
    }
}
