package EloRatingSystem.Modules.Stats.Controllers;

import EloRatingSystem.Modules.Stats.Dtos.TeamStatisticsResponseDto;
import EloRatingSystem.Modules.Stats.Services.TeamStatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/stat")
public class StatController {

    @Autowired
    TeamStatsService teamStatsServiceStatsService;

    @GetMapping("/team/all")
    public Mono<List<TeamStatisticsResponseDto>> getAllTeamStatistics() {
        return teamStatsServiceStatsService.getAllTeamStatistics();
    }
    @GetMapping("/team/{id}")
    public Mono<TeamStatisticsResponseDto> getAllTeamStatistics(@PathVariable Long id) {
        return teamStatsServiceStatsService.getTeamStatisticsByTeamPair(id);
    }

}
