package com.example.smartchess.controller.api;

import com.example.smartchess.dto.AiRequestDto;
import com.example.smartchess.dto.ChessMoveDto;
import com.example.smartchess.dto.DarkAiFlipRequestDto;
import com.example.smartchess.dto.DarkBoardStateDto;
import com.example.smartchess.dto.DarkFlipRequestDto;
import com.example.smartchess.dto.DarkMoveRequestDto;
import com.example.smartchess.service.dark.DarkAIService;
import com.example.smartchess.service.dark.DarkBoardService;
import com.example.smartchess.service.dark.DarkMoveService;
import com.example.smartchess.service.dark.DarkRuleService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dark")
public class DarkApiController {

    private final DarkBoardService boardService;
    private final DarkMoveService moveService;
    private final DarkAIService aiService;
    private final DarkRuleService ruleService;

    public DarkApiController(
            DarkBoardService boardService,
            DarkMoveService moveService,
            DarkAIService aiService,
            DarkRuleService ruleService) {

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
        state.setGameOver(false);
        state.setWinner(null);

        return state;
    }

    @PostMapping("/flip")
    public ResponseEntity<DarkBoardStateDto> flip(
            @RequestBody DarkFlipRequestDto request) {

        try {

            boolean[][] updatedRevealed =
                    moveService.flipPiece(
                            request.getRevealed(),
                            request.getX(),
                            request.getY());

            DarkBoardStateDto state = new DarkBoardStateDto();
            state.setPieces(request.getPieces());
            state.setRevealed(updatedRevealed);

            String player = request.getCurrentPlayer();
            if (player == null || player.isBlank()) {
                player = "red";
            }

            String next = nextPlayer(player);

            state.setCurrentPlayer(next);
            state.setMessage("玩家翻棋成功");
            checkGameOver(
                state,
                request.getPieces(),
                updatedRevealed,
                next,
                player);

            return ResponseEntity.ok(state);

        } catch (IllegalArgumentException ex) {

            DarkBoardStateDto error = new DarkBoardStateDto();
            error.setPieces(request.getPieces());
            error.setRevealed(request.getRevealed());
            error.setCurrentPlayer(request.getCurrentPlayer());
            error.setMessage(ex.getMessage());

            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/move")
    public ResponseEntity<DarkBoardStateDto> move(
            @RequestBody DarkMoveRequestDto request) {

        try {

            String currentPlayer =
                    request.getCurrentPlayer() == null
                            ? "red"
                            : request.getCurrentPlayer();

            String next = nextPlayer(currentPlayer);

            String[][] updatedPieces =
                    moveService.applyMove(
                            request.getPieces(),
                            request.getRevealed(),
                            request.getFromX(),
                            request.getFromY(),
                            request.getToX(),
                            request.getToY());

            DarkBoardStateDto state = new DarkBoardStateDto();
            state.setPieces(updatedPieces);
            state.setRevealed(request.getRevealed());
            state.setCurrentPlayer(next);
            state.setMessage("暗棋走法成功");

            checkGameOver(
                state,
                updatedPieces,
                request.getRevealed(),
                next,
                currentPlayer);

            return ResponseEntity.ok(state);

        } catch (IllegalArgumentException ex) {

            DarkBoardStateDto error = new DarkBoardStateDto();
            error.setPieces(request.getPieces());
            error.setRevealed(request.getRevealed());
            error.setMessage(ex.getMessage());

            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/aiflip")
    public ResponseEntity<DarkBoardStateDto> aiFlip(
            @RequestBody DarkAiFlipRequestDto request) {

        DarkBoardStateDto state =
                aiService.aiFlip(
                        request.getPieces(),
                        request.getRevealed(),
                        request.getCurrentPlayer());

        if (state == null) {
            return ResponseEntity.ok(new DarkBoardStateDto());
        }

        return ResponseEntity.ok(state);
    }

    private String nextPlayer(String currentPlayer) {
    return "red".equalsIgnoreCase(currentPlayer)
            ? "black"
            : "red";
}

private void checkGameOver(
        DarkBoardStateDto state,
        String[][] pieces,
        boolean[][] revealed,
        String nextPlayer,
        String lastPlayer) {

    boolean hasHiddenPiece = false;

    for (int y = 0; y < revealed.length; y++) {
        for (int x = 0; x < revealed[y].length; x++) {
            if (!revealed[y][x]) {
                hasHiddenPiece = true;
                break;
            }
        }

        if (hasHiddenPiece) {
            break;
        }
    }

    // 還有未翻開棋子，遊戲繼續
    if (hasHiddenPiece) {
        state.setGameOver(false);
        state.setWinner(null);
        return;
    }

    // 全部翻開後，檢查下一位是否還有合法走法
    boolean nextPlayerCanMove =
            !ruleService
                    .findLegalMoves(
                            pieces,
                            revealed,
                            nextPlayer)
                    .isEmpty();

    if (!nextPlayerCanMove) {
        state.setGameOver(true);
        state.setWinner(lastPlayer);

        state.setMessage(
                "遊戲結束！"
                        + ("red".equalsIgnoreCase(lastPlayer)
                        ? "紅方"
                        : "黑方")
                        + "獲勝！");
    } else {
        state.setGameOver(false);
        state.setWinner(null);
    }
}

    @PostMapping("/ai")
    public ResponseEntity<ChessMoveDto> ai(
            @RequestBody AiRequestDto request) {

        boolean[][] revealed =
                request.getRevealed() != null
                        ? request.getRevealed()
                        : new boolean[8][4];

        ChessMoveDto move =
                aiService.darkMinimaxMove(
                        request.getBoard(),
                        revealed,
                        request.getColor());

        if (move == null) {
            return ResponseEntity.ok(new ChessMoveDto());
        }

        return ResponseEntity.ok(move);
    }

    @PostMapping("/legal")
    public ResponseEntity<?> legalMoves(
            @RequestBody AiRequestDto request) {

        if (request.getBoard() == null
                || request.getColor() == null
                || request.getRevealed() == null) {

            return ResponseEntity.badRequest()
                    .body("board, color and revealed are required");
        }

        return ResponseEntity.ok(
                ruleService.findLegalMoves(
                        request.getBoard(),
                        request.getRevealed(),
                        request.getColor()));
    }
}