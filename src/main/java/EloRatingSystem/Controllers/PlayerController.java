package EloRatingSystem.Controllers;

import EloRatingSystem.Models.Player;
import EloRatingSystem.Reporitories.PlayerRepository;
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

    @GetMapping("/{id}")
    public Mono<Player> getById(@PathVariable Long id) {
        return Mono.just(playerRepository.findById(id).orElseThrow());
    }

    @GetMapping("/all")
    public Mono<List<Player>> getAll() {
        return Mono.just(playerRepository.findAll());
    }

    @PutMapping
    public Mono<Player> newPlayer(@RequestBody Player player) {
        // TODO: Make fixed start rating
        return Mono.just(playerRepository.save(player));
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
