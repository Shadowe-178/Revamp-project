package com.example.smartchess.repository.chess;

import com.example.smartchess.entity.chess.ChessMatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChessMatchRepository extends JpaRepository<ChessMatch, Long> {
}
