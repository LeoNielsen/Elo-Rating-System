package EloRatingSystem.Controllers;

import EloRatingSystem.Dtos.TeamRequestDto;
import EloRatingSystem.Dtos.TeamResponseDto;
import EloRatingSystem.Models.Team;
import EloRatingSystem.Reporitories.TeamRepository;
import EloRatingSystem.Services.TeamService;
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
    @Autowired
    TeamService teamService;

    @GetMapping("/{id}")
    public Mono<TeamResponseDto> getTeamById(@PathVariable Long id) {
        return Mono.just(new TeamResponseDto(teamRepository.findById(id).orElseThrow()));
    }
    @PutMapping
    public Mono<TeamResponseDto> newTeam(@RequestBody TeamRequestDto requestDto) {
        return teamService.newTeam(requestDto);
    }

}
