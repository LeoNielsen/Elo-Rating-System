package EloRatingSystem.Modules.Team.Controllers;

import EloRatingSystem.Exception.ApiException;
import EloRatingSystem.Modules.Team.Dtos.TeamPairResponseDto;
import EloRatingSystem.Modules.Team.Dtos.TeamResponseDto;
import EloRatingSystem.Modules.Team.Repositories.TeamRepository;
import EloRatingSystem.Modules.Team.Services.TeamService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

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
        // TODO: make service method
        return Mono.just(new TeamResponseDto(teamRepository.findById(id).orElseThrow()));
    }

    @GetMapping("/all")
    public  Mono<List<TeamResponseDto>> getAllTeams(){
       return teamService.getAllTeams();
    }
    @GetMapping("/pair/all")
    public  Mono<List<TeamPairResponseDto>> getAllPairTeams(){
       return teamService.getAllTeamPair();
    }    @GetMapping("/pair/{id}")
    public  Mono<TeamPairResponseDto> getPairTeamById(@PathVariable Long id) throws ApiException {
       return teamService.getTeamPairById(id);
    }

    @GetMapping("/gen/pair")
    @PreAuthorize("hasRole('admin')")
    public void generateTeamPair(){
        teamService.backfillTeamPairs();
    }

}
