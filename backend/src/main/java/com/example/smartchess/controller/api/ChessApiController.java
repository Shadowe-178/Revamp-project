package com.example.smartchess.controller.api;

import com.example.smartchess.dto.AiRequestDto;
import com.example.smartchess.dto.BoardStateDto;
import com.example.smartchess.dto.ChessMoveDto;
import com.example.smartchess.dto.MoveRequestDto;
import com.example.smartchess.service.chess.ChessAIService;
import com.example.smartchess.service.chess.ChessBoardService;
import com.example.smartchess.service.chess.ChessMoveService;
import com.example.smartchess.service.chess.ChessRuleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chess")
public class ChessApiController {

    private final ChessBoardService boardService;
    private final ChessMoveService moveService;
    private final ChessAIService aiService;
    private final ChessRuleService ruleService;

    public ChessApiController(ChessBoardService boardService, ChessMoveService moveService, ChessAIService aiService, ChessRuleService ruleService) {
        this.boardService = boardService;
        this.moveService = moveService;
        this.aiService = aiService;
        this.ruleService = ruleService;
    }

    @PostMapping("/init")
    public BoardStateDto init() {
        BoardStateDto state = new BoardStateDto();
        state.setBoard(boardService.initializeBoard());
        state.setCurrentPlayer("red");
        state.setMessage("中國象棋棋盤已初始化");
        return state;
    }

    @PostMapping("/move")
    public ResponseEntity<BoardStateDto> move(@RequestBody MoveRequestDto request) {
        try {
            String[][] updatedBoard = moveService.applyMoveForColor(request.getBoard(), request.getFrom(), request.getTo(), request.getColor());
            BoardStateDto state = new BoardStateDto();
            state.setBoard(updatedBoard);
            String nextPlayer = "red".equalsIgnoreCase(request.getColor()) ? "black" : "red";
            boolean check = ruleService.isInCheck(updatedBoard, nextPlayer);
            boolean checkmate = ruleService.isCheckmate(updatedBoard, nextPlayer);
            state.setCurrentPlayer(nextPlayer);
            state.setCheck(check);
            state.setGameOver(checkmate);
            state.setWinner(checkmate ? ("red".equals(nextPlayer) ? "black" : "red") : null);
            state.setMessage("走法成功");
            return ResponseEntity.ok(state);
        } catch (IllegalArgumentException ex) {
            BoardStateDto error = new BoardStateDto();
            error.setBoard(request.getBoard());
            error.setMessage(ex.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/ai")
    public ResponseEntity<ChessMoveDto> ai(@RequestBody AiRequestDto request) {
        ChessMoveDto move;
        String level = request.getLevel() == null ? "random" : request.getLevel();
        switch (level.toLowerCase()) {
            case "minimax":
                move = aiService.minimaxMove(request.getBoard(), request.getColor());
                break;
            case "alphabeta":
                move = aiService.alphaBetaMove(request.getBoard(), request.getColor());
                break;
            default:
                move = aiService.randomMove(request.getBoard(), request.getColor());
        }
        if (move == null) {
            return ResponseEntity.ok(new ChessMoveDto());
        }
        return ResponseEntity.ok(move);
    }

    @PostMapping("/legal")
    public ResponseEntity<?> legalMoves(@RequestBody AiRequestDto request) {
        if (request.getBoard() == null || request.getColor() == null) {
            return ResponseEntity.badRequest().body("board and color required");
        }
        return ResponseEntity.ok(ruleService.findLegalMoves(request.getBoard(), request.getColor()));
    }
}
