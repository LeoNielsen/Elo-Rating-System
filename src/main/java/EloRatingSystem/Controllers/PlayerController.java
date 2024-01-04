package EloRatingSystem.Controllers;

import EloRatingSystem.Dtos.PlayerRequestDto;
import EloRatingSystem.Dtos.PlayerResponseDto;
import EloRatingSystem.Models.Player;
import EloRatingSystem.Reporitories.PlayerRepository;
import EloRatingSystem.Services.PlayerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;


@RestController
@RequestMapping("player")
@Slf4j
public class PlayerController {

    @Autowired
    PlayerRepository playerRepository;
    @Autowired
    PlayerService playerService;

    @GetMapping("/{id}")
    public Mono<PlayerResponseDto> getById(@PathVariable Long id) {
        return Mono.just(new PlayerResponseDto(playerRepository.findById(id).orElseThrow()));
    }

    @GetMapping("/all")
    // TODO: fix Dto PLayer list
    public Mono<List<Player>> getAll() {
        return Mono.just(playerRepository.findAll());
    }

    @PutMapping
    public Mono<PlayerResponseDto> newPlayer(@RequestBody PlayerRequestDto requestDto) {
        return playerService.newPlayer(requestDto);
    }

    @DeleteMapping("/all")
    public void deletePlayers() {
        playerRepository.deleteAll();
    }
    @DeleteMapping("/{id}")
    public void deletePlayerById(@PathVariable Long id) {
        playerRepository.deleteById(id);
    }

}
