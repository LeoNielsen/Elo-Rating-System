package EloRatingSystem.Controllers;

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
@CrossOrigin
public class RatingController {

    @Autowired
    RatingService ratingService;

    @GetMapping("/match/{id}")
    public Mono<List<RatingResponseDto>> getRatingByMatchId(@PathVariable Long id) {
        return ratingService.getRatingByMatchId(id);
    }

}
