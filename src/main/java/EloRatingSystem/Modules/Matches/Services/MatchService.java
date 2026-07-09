package EloRatingSystem.Modules.Matches.Services;

import EloRatingSystem.Exception.ApiException;
import EloRatingSystem.Modules.Matches.Dtos.TeamMatchRequestDto;
import EloRatingSystem.Modules.Matches.Dtos.TeamMatchResponseDto;
import EloRatingSystem.Modules.Matches.Models.Match;
import EloRatingSystem.Modules.Matches.Repositories.MatchRepository;
import EloRatingSystem.Modules.Rating.Services.MonthlyRatingService;
import EloRatingSystem.Modules.Rating.Services.RatingService;
import EloRatingSystem.Modules.Rating.Services.TeamRatingService;
import EloRatingSystem.Modules.Stats.Dtos.MatchStatisticsDto;
import EloRatingSystem.Modules.Team.Models.Team;
import EloRatingSystem.Modules.Team.Services.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.sql.Date;
import java.time.DayOfWeek;
import java.util.*;

@Service
public class MatchService {

    @Autowired
    MatchRepository matchRepository;
    @Autowired
    TeamService teamService;

    @Autowired
    RatingService ratingService;
    @Autowired
    TeamRatingService teamRatingService;
    @Autowired
    MonthlyRatingService monthlyRatingService;


    public Mono<List<TeamMatchResponseDto>> getAllMatches() {
        List<Match> matches = matchRepository.findAll();
        List<TeamMatchResponseDto> matchResponseDtoList = new ArrayList<>();
        for (Match match : matches) {
            matchResponseDtoList.add(new TeamMatchResponseDto(match));
        }

        return Mono.just(matchResponseDtoList);
    }

    public Mono<List<TeamMatchResponseDto>> getRecentMatches() {
        List<Match> matches = matchRepository.findTop100ByOrderByIdDesc();
        List<TeamMatchResponseDto> matchResponseDtoList = new ArrayList<>();
        for (Match match : matches) {
            matchResponseDtoList.add(new TeamMatchResponseDto(match));
        }

        return Mono.just(matchResponseDtoList);
    }

    public Mono<TeamMatchResponseDto> getMatchById(Long id) {
        Optional<Match> match = matchRepository.findById(id);
        return match.map(value -> Mono.just(new TeamMatchResponseDto(value)))
                .orElseGet(() -> Mono.error(new ApiException(String.format("match %s doesn't exist", id), HttpStatus.BAD_REQUEST)));
    }

    public Mono<TeamMatchResponseDto> newMatch(TeamMatchRequestDto requestDto, String username) {
        try {
            Team redTeam = teamService.getTeam(requestDto.getRedAtkId(), requestDto.getRedDefId());
            Team blueTeam = teamService.getTeam(requestDto.getBlueAtkId(), requestDto.getBlueDefId());

            Match match = matchRepository.save(new Match(new Date(System.currentTimeMillis()), redTeam, blueTeam,
                    requestDto.getRedScore(), requestDto.getBlueScore(),username));

            match = ratingService.newRating(match);
            monthlyRatingService.newRating(match);
            teamRatingService.newTeamRating(match);
            match = matchRepository.save(match);

            return Mono.just(new TeamMatchResponseDto(match));
        } catch (ApiException e) {
            return Mono.error(e);
        }
    }



    public Mono<MatchStatisticsDto> getStatistics() {

        int redWins = 0;
        int blueWins = 0;
        int redGoals = 0;
        int blueGoals = 0;

        List<Match> matches = matchRepository.findAll();
        for (Match match : matches) {
            if (match.getRedTeamScore() == 10) {
                redWins++;
            } else {
                blueWins++;
            }
            redGoals += match.getRedTeamScore();
            blueGoals += match.getBlueTeamScore();
        }

        return Mono.just(new MatchStatisticsDto(redWins, blueWins, redGoals, blueGoals));
    }

    public Map<String, Integer> getMatchDays() {
        List<Match> matches = matchRepository.findAll();

        Map<String, Integer> winsByDay = new HashMap<>();

        for (Match match : matches) {
            DayOfWeek day = match.getDate().toLocalDate().getDayOfWeek();
            String dayName = day.toString();
            winsByDay.put(dayName, winsByDay.getOrDefault(dayName, 0) + 1);
        }
        return winsByDay;
    }

}
