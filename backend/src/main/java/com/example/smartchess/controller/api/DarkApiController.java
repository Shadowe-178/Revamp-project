package com.example.smartchess.controller.api;

import com.example.smartchess.dto.AiRequestDto;
import com.example.smartchess.dto.ChessMoveDto;
import com.example.smartchess.dto.DarkBoardStateDto;
import com.example.smartchess.dto.DarkFlipRequestDto;
import com.example.smartchess.dto.DarkMoveRequestDto;
import com.example.smartchess.service.dark.DarkAIService;
import com.example.smartchess.service.dark.DarkBoardService;
import com.example.smartchess.service.dark.DarkMoveService;
import com.example.smartchess.service.dark.DarkRuleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dark")
public class DarkApiController {

    private final DarkBoardService boardService;
    private final DarkMoveService moveService;
    private final DarkAIService aiService;
    private final DarkRuleService ruleService;

    public DarkApiController(DarkBoardService boardService, DarkMoveService moveService, DarkAIService aiService, DarkRuleService ruleService) {
        this.boardService = boardService;
        this.moveService = moveService;
        this.aiService = aiService;
        this.ruleService = ruleService;
    }

    @PostMapping("/init")
    public DarkBoardStateDto init() {
        DarkBoardStateDto state = new DarkBoardStateDto();
        state.setPieces(boardService.initializeBoard());
        state.setRevealed(new boolean[8][4]);
        state.setCurrentPlayer("red");
        state.setMessage("暗棋棋盤已初始化");
        return state;
    }

    @PostMapping("/flip")
    public ResponseEntity<DarkBoardStateDto> flip(@RequestBody DarkFlipRequestDto request) {
        try {
            boolean[][] updatedRevealed = moveService.flipPiece(request.getRevealed(), request.getX(), request.getY());
            DarkBoardStateDto state = new DarkBoardStateDto();
            state.setPieces(request.getPieces());
            state.setRevealed(updatedRevealed);
            String player = request.getCurrentPlayer();
            if (player == null || player.isBlank()) {
                player = request.getPieces()[request.getY()][request.getX()].startsWith("r") ? "red" : "black";
            }
            state.setCurrentPlayer(nextPlayer(player));
            state.setMessage("翻棋成功");
            return ResponseEntity.ok(state);
        } catch (IllegalArgumentException ex) {
            DarkBoardStateDto error = new DarkBoardStateDto();
            error.setPieces(request.getPieces());
            error.setRevealed(request.getRevealed());
            error.setMessage(ex.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/move")
    public ResponseEntity<DarkBoardStateDto> move(@RequestBody DarkMoveRequestDto request) {
        try {
            String currentPlayer = request.getCurrentPlayer() == null ? "red" : request.getCurrentPlayer();
            String[][] updatedPieces = moveService.applyMove(request.getPieces(), request.getRevealed(), request.getFromX(), request.getFromY(), request.getToX(), request.getToY());
            DarkBoardStateDto state = new DarkBoardStateDto();
            state.setPieces(updatedPieces);
            state.setRevealed(request.getRevealed());
            state.setCurrentPlayer(nextPlayer(currentPlayer));
            state.setMessage("暗棋走法成功");
            return ResponseEntity.ok(state);
        } catch (IllegalArgumentException ex) {
            DarkBoardStateDto error = new DarkBoardStateDto();
            error.setPieces(request.getPieces());
            error.setRevealed(request.getRevealed());
            error.setMessage(ex.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    private String nextPlayer(String currentPlayer) {
        return "red".equalsIgnoreCase(currentPlayer) ? "black" : "red";
    }

    @PostMapping("/ai")
    public ResponseEntity<ChessMoveDto> ai(@RequestBody AiRequestDto request) {
        boolean[][] revealed = request.getRevealed() != null ? request.getRevealed() : new boolean[8][4];
        ChessMoveDto move = aiService.darkMinimaxMove(request.getBoard(), revealed, request.getColor());
        if (move == null) {
            return ResponseEntity.ok(new ChessMoveDto());
        }
        return ResponseEntity.ok(move);
    }

    @PostMapping("/legal")
    public ResponseEntity<?> legalMoves(@RequestBody AiRequestDto request) {
        if (request.getBoard() == null || request.getColor() == null || request.getRevealed() == null) {
            return ResponseEntity.badRequest().body("board, color and revealed are required");
        }
        return ResponseEntity.ok(ruleService.findLegalMoves(request.getBoard(), request.getRevealed(), request.getColor()));
    }
}
