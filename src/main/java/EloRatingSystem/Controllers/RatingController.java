package EloRatingSystem.Controllers;

import EloRatingSystem.Dtos.PlayerDtos.ChartDataDto;
import EloRatingSystem.Dtos.RatingResponseDto;
import EloRatingSystem.Services.RatingServices.RatingService;
import EloRatingSystem.Services.RatingServices.SoloRatingService;
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

}
