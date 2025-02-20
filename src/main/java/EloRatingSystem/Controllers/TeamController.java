package EloRatingSystem.Controllers;

import EloRatingSystem.Dtos.TeamRequestDto;
import EloRatingSystem.Dtos.TeamResponseDto;
import EloRatingSystem.Reporitories.TeamRepository;
import EloRatingSystem.Services.TeamService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("team")
@Slf4j
@CrossOrigin(origins = "*")
public class TeamController {

    @Autowired
    TeamRepository teamRepository;
    @Autowired
    TeamService teamService;

    @GetMapping("/{id}")
    public Mono<TeamResponseDto> getTeamById(@PathVariable Long id) {
        // TODO: make service method
        return Mono.just(new TeamResponseDto(teamRepository.findById(id).orElseThrow()));
    }

    @GetMapping("/all")
    public  Mono<List<TeamResponseDto>> getAllTeams(){
       return teamService.getAllTeams();
    }

    @PostMapping
    public Mono<TeamResponseDto> newTeam(@RequestBody TeamRequestDto requestDto) {
        return teamService.newTeam(requestDto);
    }

}
