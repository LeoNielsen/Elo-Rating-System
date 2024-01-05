package EloRatingSystem.Controllers;

import EloRatingSystem.Dtos.PlayerRequestDto;
import EloRatingSystem.Dtos.PlayerResponseDto;
import EloRatingSystem.Exception.ApiException;
import EloRatingSystem.Models.Player;
import EloRatingSystem.Reporitories.PlayerRepository;
import EloRatingSystem.Services.PlayerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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

    @GetMapping("/id/{id}")
    public Mono<PlayerResponseDto> getById(@PathVariable Long id) {
        return Mono.just(new PlayerResponseDto(playerRepository.findById(id).orElseThrow()));
    }

    @GetMapping("/{nameTag}")
    public Mono<PlayerResponseDto> getByNameTag(@PathVariable String nameTag) {
            return playerService.getByNameTag(nameTag);
    }

    @GetMapping("/all")
    public Mono<List<PlayerResponseDto>> getAll() {
        return playerService.getAllPlayers();
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
