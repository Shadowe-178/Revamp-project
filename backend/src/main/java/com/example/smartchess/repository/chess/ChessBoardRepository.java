package com.example.smartchess.repository.chess;

import com.example.smartchess.entity.chess.ChessBoard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChessBoardRepository extends JpaRepository<ChessBoard, Long> {
}
