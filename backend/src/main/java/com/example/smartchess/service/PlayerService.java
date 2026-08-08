package com.example.smartchess.service;

import com.example.smartchess.dto.PlayerLoginDto;
import com.example.smartchess.dto.PlayerProfileDto;
import com.example.smartchess.dto.PlayerRegisterDto;
import com.example.smartchess.entity.Player;
import com.example.smartchess.repository.PlayerRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PlayerService {

    private final PlayerRepository playerRepository;

    public PlayerService(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    public Player register(PlayerRegisterDto dto) {
        if (playerRepository.existsByUsername(dto.getUsername())) {
            throw new IllegalArgumentException("帳號已存在");
        }
        Player player = new Player();
        player.setUsername(dto.getUsername());
        player.setPassword(dto.getPassword());
        player.setDisplayName(dto.getDisplayName());
        player.setTotalGames(0);
        player.setWinCount(0);
        player.setChessGames(0);
        player.setDarkGames(0);
        return playerRepository.save(player);
    }

    public Player login(PlayerLoginDto dto) {
        Optional<Player> playerOptional = playerRepository.findByUsername(dto.getUsername());
        if (playerOptional.isEmpty()) {
            throw new IllegalArgumentException("使用者不存在");
        }
        Player player = playerOptional.get();
        if (!player.getPassword().equals(dto.getPassword())) {
            throw new IllegalArgumentException("密碼錯誤");
        }
        return player;
    }

    public PlayerProfileDto getProfile(String username) {
        Player player = playerRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("使用者不存在"));
        PlayerProfileDto profile = new PlayerProfileDto();
        profile.setUsername(player.getUsername());
        profile.setDisplayName(player.getDisplayName());
        profile.setTotalGames(player.getTotalGames());
        profile.setWinCount(player.getWinCount());
        profile.setChessGames(player.getChessGames());
        profile.setDarkGames(player.getDarkGames());
        return profile;
    }
}
