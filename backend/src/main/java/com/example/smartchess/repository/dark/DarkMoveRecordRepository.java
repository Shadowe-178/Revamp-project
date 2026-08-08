package com.example.smartchess.repository.dark;

import com.example.smartchess.entity.dark.DarkMoveRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DarkMoveRecordRepository extends JpaRepository<DarkMoveRecord, Long> {
}
