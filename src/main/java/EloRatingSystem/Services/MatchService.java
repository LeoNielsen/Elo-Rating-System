package EloRatingSystem.Services;

import EloRatingSystem.Dtos.MatchRequestDto;
import EloRatingSystem.Dtos.MatchResponseDto;
import EloRatingSystem.Exception.ApiException;
import EloRatingSystem.Models.Match;
import EloRatingSystem.Models.Team;
import EloRatingSystem.Reporitories.MatchRepository;
import EloRatingSystem.Reporitories.TeamRepository;
import EloRatingSystem.Services.RatingServices.MonthlyRatingService;
import EloRatingSystem.Services.RatingServices.RatingService;
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
    RatingService ratingService;
    @Autowired
    MonthlyRatingService monthlyRatingService;
    @Autowired
    PlayerService playerService;

    public Mono<List<MatchResponseDto>> getRecentMatches() {
        List<Match> matches = matchRepository.findTop100ByOrderByIdDesc();
        List<MatchResponseDto> matchResponseDtoList = new ArrayList<>();
        for (Match match : matches) {
            matchResponseDtoList.add(new MatchResponseDto(match));
        }

        return Mono.just(matchResponseDtoList);
    }

    public Mono<MatchResponseDto> getMatchById(Long id) {
        Optional<Match> match = matchRepository.findById(id);
        return match.map(value -> Mono.just(new MatchResponseDto(value)))
                .orElseGet(() -> Mono.error(new ApiException(String.format("match %s doesn't exist", id), HttpStatus.BAD_REQUEST)));
    }

    public Mono<MatchResponseDto> newMatch(MatchRequestDto requestDto) {
        Optional<Team> redTeamOptional = teamRepository.findById(requestDto.getRedTeamId());
        Optional<Team> blueTeamOptional = teamRepository.findById(requestDto.getBlueTeamId());

        if (blueTeamOptional.isPresent() && redTeamOptional.isPresent()) {
            Team redTeam = redTeamOptional.get();
            Team blueTeam = blueTeamOptional.get();

            Match match = matchRepository.save(new Match(new Date(System.currentTimeMillis()), redTeam, blueTeam,
                    requestDto.getRedTeamScore(), requestDto.getBlueTeamScore()));

            match = ratingService.newRating(match);
            monthlyRatingService.newRating(match);
            match = matchRepository.save(match);

            return Mono.just(new MatchResponseDto(match));
        }

        return Mono.error(new ApiException(
                String.format("Either team %s or %s does not exits", requestDto.getRedTeamId(), requestDto.getBlueTeamId())
                , HttpStatus.BAD_REQUEST));
    }

    public void deleteLatestMatch() {
        Match match = matchRepository.findTop1ByOrderByIdDesc().orElseThrow();
        ratingService.deleteRatingsByMatch(match.getId());

        Team winner = match.getBlueTeamScore() < match.getRedTeamScore() ? match.getRedTeam() : match.getBlueTeam();
        Team loser = match.getBlueTeamScore() < match.getRedTeamScore() ? match.getBlueTeam() : match.getRedTeam();

        winner.setWon(winner.getWon() - 1);
        loser.setLost(loser.getLost() - 1);

        teamRepository.save(winner);
        teamRepository.save(loser);
        monthlyRatingService.deleteRatingsByMatch(match.getId());
        matchRepository.deleteById(match.getId());

        playerService.regeneratePlayerStatistics(winner.getDefender());
        playerService.regeneratePlayerStatistics(loser.getDefender());
        playerService.regeneratePlayerStatistics(winner.getAttacker());
        playerService.regeneratePlayerStatistics(loser.getAttacker());
    }
}
