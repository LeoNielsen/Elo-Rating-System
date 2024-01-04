package EloRatingSystem.Controllers;

import EloRatingSystem.Models.Team;
import EloRatingSystem.Reporitories.TeamRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("team")
@Slf4j
public class TeamController {

    @Autowired
    TeamRepository teamRepository;

    @GetMapping("/{id}")
    public Mono<Team> getTeamById(@PathVariable Long id) {
        return Mono.just(teamRepository.findById(id).orElseThrow());
    }
    @PutMapping
    public Mono<Team> newMatch(@RequestBody Team team) {
        return Mono.just(teamRepository.save(team));
    }

}
