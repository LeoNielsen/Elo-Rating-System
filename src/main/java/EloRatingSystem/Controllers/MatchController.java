package EloRatingSystem.Controllers;

import EloRatingSystem.Dtos.MatchRequestDto;
import EloRatingSystem.Dtos.MatchResponseDto;
import EloRatingSystem.Reporitories.MatchRepository;
import EloRatingSystem.Services.MatchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("match")
@Slf4j
public class MatchController {

    @Autowired
    MatchService matchService;

    @GetMapping("/{id}")
    public Mono<MatchResponseDto> getMatchById(@PathVariable Long id) {
        return matchService.getMatchById(id);
    }

    @GetMapping("/all")
    public Mono<List<MatchResponseDto>> getAll() {
        return matchService.getAllMatches();
    }

    @PostMapping
    public Mono<MatchResponseDto> newMatch(@RequestBody MatchRequestDto requestDto) {
        return matchService.newMatch(requestDto);
    }

    @DeleteMapping("/latest")
    public void deleteLatestMatch() {
        matchService.deleteLatestMatch();
    }

}
