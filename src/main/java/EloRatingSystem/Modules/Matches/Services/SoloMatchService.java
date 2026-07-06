package EloRatingSystem.Modules.Matches.Services;

import EloRatingSystem.Exception.ApiException;
import EloRatingSystem.Modules.Matches.Dtos.SoloMatchRequestDto;
import EloRatingSystem.Modules.Matches.Dtos.SoloMatchResponseDto;
import EloRatingSystem.Modules.Matches.Models.SoloMatch;
import EloRatingSystem.Modules.Matches.Repositories.SoloMatchRepository;
import EloRatingSystem.Modules.Rating.Services.SoloRatingService;
import EloRatingSystem.Modules.Stats.Dtos.MatchStatisticsDto;
import EloRatingSystem.Modules.player.Models.Player;
import EloRatingSystem.Modules.player.Repositories.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.sql.Date;
import java.time.DayOfWeek;
import java.util.*;

@Service
public class SoloMatchService {
    @Autowired
    SoloMatchRepository soloMatchRepository;
    @Autowired
    PlayerRepository playerRepository;
    @Autowired
    SoloRatingService soloRatingService;


    public Mono<List<SoloMatchResponseDto>> getRecentMatches() {
        List<SoloMatch> matches = soloMatchRepository.findTop100ByOrderByIdDesc();
        List<SoloMatchResponseDto> matchResponseDtoList = new ArrayList<>();
        for (SoloMatch match : matches) {
            matchResponseDtoList.add(new SoloMatchResponseDto(match));
        }

        return Mono.just(matchResponseDtoList);
    }

    public Mono<SoloMatchResponseDto> getSoloMatchById(Long id) {
        Optional<SoloMatch> match = soloMatchRepository.findById(id);
        return match.map(value -> Mono.just(new SoloMatchResponseDto(value)))
                .orElseGet(() -> Mono.error(new ApiException(String.format("match %s doesn't exist", id), HttpStatus.BAD_REQUEST)));

    }

    public Mono<SoloMatchResponseDto> newSoloMatch(SoloMatchRequestDto requestDto,String username) {
        Optional<Player> redPlayerOptional = playerRepository.findById(requestDto.getRedPlayerId());
        Optional<Player> bluePlayerOptional = playerRepository.findById(requestDto.getBluePlayerId());

        if (bluePlayerOptional.isPresent() && redPlayerOptional.isPresent()) {
            Player redPlayer = redPlayerOptional.get();
            Player bluePlayer = bluePlayerOptional.get();

            SoloMatch match = soloMatchRepository.save(new SoloMatch(new Date(System.currentTimeMillis()), redPlayer, bluePlayer,
                    requestDto.getRedScore(), requestDto.getBlueScore(),username));

            match = soloRatingService.newSoloRating(match);

            match = soloMatchRepository.save(match);

            return Mono.just(new SoloMatchResponseDto(match));
        }

        return Mono.error(new ApiException(
                String.format("Either player %s or %s does not exits", requestDto.getRedPlayerId(), requestDto.getBluePlayerId())
                , HttpStatus.BAD_REQUEST));
    }


    public Mono<MatchStatisticsDto> getSoloStatistics() {

        int redWins = 0;
        int blueWins = 0;
        int redGoals = 0;
        int blueGoals = 0;

        List<SoloMatch> matches = soloMatchRepository.findAll();
        for (SoloMatch match : matches) {
            if (match.getRedScore() == 10) {
                redWins++;
            } else {
                blueWins++;
            }
            redGoals += match.getRedScore();
            blueGoals += match.getBlueScore();
        }

        return Mono.just(new MatchStatisticsDto(redWins, blueWins, redGoals, blueGoals));

    }

    public Map<String, Integer> getMatchDays() {
        List<SoloMatch> matches = soloMatchRepository.findAll();

        Map<String, Integer> winsByDay = new HashMap<>();

        for (SoloMatch match : matches) {
            DayOfWeek day = match.getDate().toLocalDate().getDayOfWeek();
            String dayName = day.toString();
            winsByDay.put(dayName, winsByDay.getOrDefault(dayName, 0) + 1);
        }
        return winsByDay;
    }
}
