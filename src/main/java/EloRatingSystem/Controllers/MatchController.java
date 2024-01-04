package EloRatingSystem.Controllers;

import EloRatingSystem.Models.Match;
import EloRatingSystem.Reporitories.MatchRepository;
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

    @GetMapping("/{id}")
    public Mono<Match> getMatchById(@PathVariable Long id) {
        return Mono.just(matchRepository.findById(id).orElseThrow());
    }
    @PutMapping
    public Mono<Match> newMatch(@RequestBody Match match) {
        return Mono.just(matchRepository.save(match));
    }

}
