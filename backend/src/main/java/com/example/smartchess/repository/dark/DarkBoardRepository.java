package com.example.smartchess.repository.dark;

import com.example.smartchess.entity.dark.DarkBoard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DarkBoardRepository extends JpaRepository<DarkBoard, Long> {
}
