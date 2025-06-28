package EloRatingSystem.Controllers;

import EloRatingSystem.Dtos.PlayerDtos.PlayerRequestDto;
import EloRatingSystem.Dtos.PlayerDtos.PlayerResponseDto;
import EloRatingSystem.Dtos.PlayerDtos.PlayerStatisticsResponseDto;
import EloRatingSystem.Dtos.SoloPlayerStatisticsResponseDto;
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

    @GetMapping("/all")
    public Mono<List<PlayerResponseDto>> getAll() {
        return playerService.getAllPlayers();
    }

    @GetMapping("/id/{id}")
    public Mono<PlayerResponseDto> getById(@PathVariable Long id) {
        // TODO: make service method
        return Mono.just(new PlayerResponseDto(playerRepository.findById(id).orElseThrow()));
    }

    @GetMapping("/{nameTag}")
    public Mono<PlayerResponseDto> getByNameTag(@PathVariable String nameTag) {
        return playerService.getByNameTag(nameTag);
    }

    @PostMapping
    public Mono<PlayerResponseDto> newPlayer(@RequestBody PlayerRequestDto requestDto) {
        return playerService.newPlayer(requestDto);
    }

    @GetMapping("/statistics/{id}")
    public Mono<PlayerStatisticsResponseDto> getStatisticsById(@PathVariable Long id) {
        return playerService.getStatistics(id);
    }

    @GetMapping("/statistics/all")
    public Mono<List<PlayerStatisticsResponseDto>> getAllStatistics() {
        return playerService.getAllStatistics();
    }

    @GetMapping("statistics/monthly/all")
    public Mono<List<PlayerStatisticsResponseDto>> getAllMonthlyStatistics() {
        return playerService.getAllMonthlyStatistics();
    }
    @GetMapping("/statistics/monthly/{id}")
    public Mono<PlayerStatisticsResponseDto> getStatisticsMonthlyById(@PathVariable Long id) {
        return playerService.getMonthlyStatistics(id);
    }

    @GetMapping("/statistics/solo/{id}")
    public Mono<SoloPlayerStatisticsResponseDto> getSoloStatisticsById(@PathVariable Long id) {
        return playerService.getSoloStatistics(id);
    }

    @GetMapping("/statistics/solo/all")
    public Mono<List<SoloPlayerStatisticsResponseDto>> getAllSoloStatistics() {
        return playerService.getAllSoloStatistics();
    }


}
