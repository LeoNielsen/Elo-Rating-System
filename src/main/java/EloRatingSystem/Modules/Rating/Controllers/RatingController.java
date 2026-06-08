package EloRatingSystem.Modules.Rating.Controllers;

import EloRatingSystem.Modules.Stats.Dtos.ChartDataDto;
import EloRatingSystem.Modules.Rating.Dtos.RatingResponseDto;
import EloRatingSystem.Modules.Rating.Services.MonthlyRatingService;
import EloRatingSystem.Modules.Rating.Services.RatingService;
import EloRatingSystem.Modules.Rating.Services.SoloRatingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("rating")
@Slf4j
public class RatingController {

    @Autowired
    RatingService ratingService;
    @Autowired
    SoloRatingService soloRatingService;
    @Autowired
    MonthlyRatingService monthlyRatingService;

    @GetMapping("/match/{id}")
    public Mono<List<RatingResponseDto>> getRatingByMatchId(@PathVariable Long id) {
        return ratingService.getRatingByMatchId(id);
    }

    @GetMapping("/solo/match/{id}")
    public Mono<List<RatingResponseDto>> getSoloRatingByMatchId(@PathVariable Long id) {
        return soloRatingService.getSoloRatingBySoloMatchId(id);
    }

    @GetMapping("/chart")
    public Mono<List<ChartDataDto>> getChartData() {
        return ratingService.getChartData();
    }
    @GetMapping("/solo/chart")
    public Mono<List<ChartDataDto>> getSoloChartData() {
        return soloRatingService.getSoloChartData();
    }
    @GetMapping("/monthly/chart")
    public Mono<List<ChartDataDto>> getMonthlyChartData() {
        return monthlyRatingService.getChartData();
    }

}
