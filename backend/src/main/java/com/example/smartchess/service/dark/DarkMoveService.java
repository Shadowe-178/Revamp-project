package com.example.smartchess.service.dark;

import org.springframework.stereotype.Service;

@Service
public class DarkMoveService {

    private final DarkRuleService ruleService;

    public DarkMoveService(DarkRuleService ruleService) {
        this.ruleService = ruleService;
    }

    public boolean[][] flipPiece(boolean[][] revealed, int x, int y) {
        if (!ruleService.isLegalFlip(revealed, x, y)) {
            throw new IllegalArgumentException("無效的翻棋位置");
        }
        return ruleService.flipPiece(revealed, x, y);
    }

    public String[][] applyMove(String[][] pieces, boolean[][] revealed, int fromX, int fromY, int toX, int toY) {
        if (!ruleService.isLegalMove(pieces, revealed, fromX, fromY, toX, toY)) {
            throw new IllegalArgumentException("不合法的暗棋走法");
        }
        return ruleService.applyMove(pieces, fromX, fromY, toX, toY);
    }
}
