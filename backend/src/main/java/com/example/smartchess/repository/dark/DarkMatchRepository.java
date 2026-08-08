package com.example.smartchess.repository.dark;

import com.example.smartchess.entity.dark.DarkMatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DarkMatchRepository extends JpaRepository<DarkMatch, Long> {
}
