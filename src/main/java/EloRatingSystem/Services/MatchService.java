package EloRatingSystem.Services;

import EloRatingSystem.Dtos.Match2v2ResponseDto;
import EloRatingSystem.Dtos.MatchRequestDto;
import EloRatingSystem.Exception.ApiException;
import EloRatingSystem.Models.Achievement.GameType;
import EloRatingSystem.Models.Match;
import EloRatingSystem.Models.Team;
import EloRatingSystem.Reporitories.Achievements.PlayerAchievementRepository;
import EloRatingSystem.Reporitories.MatchRepository;
import EloRatingSystem.Reporitories.TeamRepository;
import EloRatingSystem.Services.RatingServices.MonthlyRatingService;
import EloRatingSystem.Services.RatingServices.RatingService;
import jakarta.transaction.Transactional;
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

            return Mono.just(new Match2v2ResponseDto(match));
        }

        return Mono.error(new ApiException(
                String.format("Either team %s or %s does not exits", requestDto.getRedTeamId(), requestDto.getBlueTeamId())
                , HttpStatus.BAD_REQUEST));
    }

    @Transactional
    public void deleteLatestMatch() {
        Match match = matchRepository.findTop1ByOrderByIdDesc().orElseThrow();
        ratingService.deleteRatingsByMatch(match.getDate().toLocalDate(),match.getId());

        Team winner = match.getBlueTeamScore() < match.getRedTeamScore() ? match.getRedTeam() : match.getBlueTeam();
        Team loser = match.getBlueTeamScore() < match.getRedTeamScore() ? match.getBlueTeam() : match.getRedTeam();

        winner.setWon(winner.getWon() - 1);
        loser.setLost(loser.getLost() - 1);

        teamRepository.save(winner);
        teamRepository.save(loser);
        monthlyRatingService.deleteRatingsByMatch(match.getDate().toLocalDate(),match.getId());
        matchRepository.deleteById(match.getId());

        playerAchievementRepository.deleteAllByPlayerIdAndGameType(winner.getDefender().getId(), GameType.TEAMS);
        playerAchievementRepository.deleteAllByPlayerIdAndGameType(loser.getDefender().getId(), GameType.TEAMS);
        playerAchievementRepository.deleteAllByPlayerIdAndGameType(winner.getAttacker().getId(), GameType.TEAMS);
        playerAchievementRepository.deleteAllByPlayerIdAndGameType(loser.getAttacker().getId(), GameType.TEAMS);

        regenerateService.regeneratePlayerStatistics(winner.getDefender());
        regenerateService.regeneratePlayerStatistics(loser.getDefender());
        regenerateService.regeneratePlayerStatistics(winner.getAttacker());
        regenerateService.regeneratePlayerStatistics(loser.getAttacker());

        regenerateService.regenerateMonthlyStatistics(winner.getDefender());
        regenerateService.regenerateMonthlyStatistics(loser.getDefender());
        regenerateService.regenerateMonthlyStatistics(winner.getAttacker());
        regenerateService.regenerateMonthlyStatistics(loser.getAttacker());

    }
}
