package EloRatingSystem.Modules.Matches.Services;

import EloRatingSystem.Exception.ApiException;
import EloRatingSystem.Modules.Achievement.Repositories.PlayerAchievementRepository;
import EloRatingSystem.Modules.Matches.Dtos.Match2v2ResponseDto;
import EloRatingSystem.Modules.Matches.Dtos.MatchRequestDto;
import EloRatingSystem.Modules.Matches.Models.Match;
import EloRatingSystem.Modules.Matches.Repositories.MatchRepository;
import EloRatingSystem.Modules.Rating.Services.MonthlyRatingService;
import EloRatingSystem.Modules.Rating.Services.RatingService;
import EloRatingSystem.Modules.Stats.Dtos.MatchStatisticsDto;
import EloRatingSystem.Modules.Team.Models.Team;
import EloRatingSystem.Modules.Team.Repositories.TeamRepository;
import EloRatingSystem.Modules.player.Models.Player;
import EloRatingSystem.Modules.player.Repositories.PlayerRepository;
import EloRatingSystem.Services.RegenerateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class MatchService {

    @Autowired
    MatchRepository matchRepository;
    @Autowired
    TeamRepository teamRepository;
    @Autowired
    PlayerRepository playerRepository;
    @Autowired
    RatingService ratingService;
    @Autowired
    MonthlyRatingService monthlyRatingService;
    @Autowired
    RegenerateService regenerateService;
    @Autowired
    PlayerAchievementRepository playerAchievementRepository;

    public Mono<List<Match2v2ResponseDto>> getAllMatches() {
        List<Match> matches = matchRepository.findAll();
        List<Match2v2ResponseDto> matchResponseDtoList = new ArrayList<>();
        for (Match match : matches) {
            matchResponseDtoList.add(new Match2v2ResponseDto(match));
        }

        return Mono.just(matchResponseDtoList);
    }

    public Mono<List<Match2v2ResponseDto>> getRecentMatches() {
        List<Match> matches = matchRepository.findTop100ByOrderByIdDesc();
        List<Match2v2ResponseDto> matchResponseDtoList = new ArrayList<>();
        for (Match match : matches) {
            matchResponseDtoList.add(new Match2v2ResponseDto(match));
        }

        return Mono.just(matchResponseDtoList);
    }

    public Mono<Match2v2ResponseDto> getMatchById(Long id) {
        Optional<Match> match = matchRepository.findById(id);
        return match.map(value -> Mono.just(new Match2v2ResponseDto(value)))
                .orElseGet(() -> Mono.error(new ApiException(String.format("match %s doesn't exist", id), HttpStatus.BAD_REQUEST)));
    }

    public Mono<Match2v2ResponseDto> newMatch(MatchRequestDto requestDto) {
        try {
            Team redTeam = getTeam(requestDto.getRedAtkId(), requestDto.getRedDefId());
            Team blueTeam = getTeam(requestDto.getBlueAtkId(), requestDto.getBlueDefId());

            Match match = matchRepository.save(new Match(new Date(System.currentTimeMillis()), redTeam, blueTeam,
                    requestDto.getRedScore(), requestDto.getBlueScore()));

            match = ratingService.newRating(match);
            monthlyRatingService.newRating(match);
            match = matchRepository.save(match);

            return Mono.just(new Match2v2ResponseDto(match));
        } catch (ApiException e) {
            return Mono.error(e);
        }
    }

    public Team getTeam(long atkId, long defId) throws ApiException {
        Optional<Team> teamOptional = teamRepository.findByAttackerIdAndDefenderId(atkId, defId);
        if (teamOptional.isPresent()) {
            return teamOptional.get();
        } else {
            Player atk = playerRepository.findById(atkId)
                    .orElseThrow(() -> new ApiException(String.format("player %s doesn't exist", atkId), HttpStatus.BAD_REQUEST));
            Player def = playerRepository.findById(defId)
                    .orElseThrow(() -> new ApiException(String.format("player %s doesn't exist", defId), HttpStatus.BAD_REQUEST));
            return teamRepository.save(new Team(atk, def));
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
}
