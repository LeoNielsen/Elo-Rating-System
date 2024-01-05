package EloRatingSystem.Controllers;

import EloRatingSystem.Dtos.MatchRequestDto;
import EloRatingSystem.Dtos.MatchResponseDto;
import EloRatingSystem.Models.Match;
import EloRatingSystem.Reporitories.MatchRepository;
import EloRatingSystem.Services.MatchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("match")
@Slf4j
public class MatchController {

    @Autowired
    MatchRepository matchRepository;
    @Autowired
    MatchService matchService;

    @GetMapping("/{id}")
    public Mono<MatchResponseDto> getMatchById(@PathVariable Long id) {
        return matchService.getMatchById(id);
    }
    @PutMapping
    public Mono<MatchResponseDto> newMatch(@RequestBody MatchRequestDto requestDto) {
        return matchService.newMatch(requestDto);
    }

}
