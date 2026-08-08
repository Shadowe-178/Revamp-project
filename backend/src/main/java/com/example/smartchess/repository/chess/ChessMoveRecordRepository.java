package com.example.smartchess.repository.chess;

import com.example.smartchess.entity.chess.ChessMoveRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChessMoveRecordRepository extends JpaRepository<ChessMoveRecord, Long> {
}
