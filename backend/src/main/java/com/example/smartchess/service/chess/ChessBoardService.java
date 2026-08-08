package com.example.smartchess.service.chess;

import org.springframework.stereotype.Service;

@Service
public class ChessBoardService {

    private final ChessRuleService ruleService;

    public ChessBoardService(ChessRuleService ruleService) {
        this.ruleService = ruleService;
    }

    public String[][] initializeBoard() {
        return ruleService.initializeBoard();
    }
}
