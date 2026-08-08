package com.example.smartchess.service.dark;

import org.springframework.stereotype.Service;

@Service
public class DarkBoardService {

    private final DarkRuleService ruleService;

    public DarkBoardService(DarkRuleService ruleService) {
        this.ruleService = ruleService;
    }

    public String[][] initializeBoard() {
        return ruleService.initializeBoard();
    }
}
