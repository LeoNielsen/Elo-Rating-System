package EloRatingSystem.Controllers;

import EloRatingSystem.Dtos.ChartDataDto;
import EloRatingSystem.Dtos.MatchRequestDto;
import EloRatingSystem.Dtos.MatchResponseDto;
import EloRatingSystem.Dtos.RatingResponseDto;
import EloRatingSystem.Services.MatchService;
import EloRatingSystem.Services.RatingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("rating")
@Slf4j
public class RatingController {

    @Autowired
    RatingService ratingService;

    @GetMapping("/match/{id}")
    public Mono<List<RatingResponseDto>> getRatingByMatchId(@PathVariable Long id) {
        return ratingService.getRatingByMatchId(id);
    }

    @GetMapping("/all")
    public Mono<List<RatingResponseDto>> getAll() {
        return ratingService.getAllRatings();
    }

    @GetMapping("/solo/match/{id}")
    public Mono<List<RatingResponseDto>> getSoloRatingByMatchId(@PathVariable Long id) {
        return ratingService.getSoloRatingBySoloMatchId(id);
    }

    @GetMapping("/solo/all")
    public Mono<List<RatingResponseDto>> getAllSoloRatings() {
        return ratingService.getAllSoloRatings();
    }

    @GetMapping("/chart")
    public Mono<List<ChartDataDto>> getChartData() {
        return ratingService.getChartData();
    }
    @GetMapping("/solo/chart")
    public Mono<List<ChartDataDto>> getSoloChartData() {
        return ratingService.getSoloChartData();
    }

}
