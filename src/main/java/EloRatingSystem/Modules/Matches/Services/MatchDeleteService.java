package EloRatingSystem.Modules.Matches.Services;

import EloRatingSystem.Exception.ApiException;
import EloRatingSystem.Modules.Achievement.Repositories.PlayerAchievementRepository;
import EloRatingSystem.Modules.Matches.Dtos.TeamMatchRequestDto;
import EloRatingSystem.Modules.Matches.Dtos.TeamMatchResponseDto;
import EloRatingSystem.Modules.Matches.Models.Match;
import EloRatingSystem.Modules.Matches.Models.SoloMatch;
import EloRatingSystem.Modules.Matches.Repositories.MatchRepository;
import EloRatingSystem.Modules.Matches.Repositories.SoloMatchRepository;
import EloRatingSystem.Modules.Rating.Services.MonthlyRatingService;
import EloRatingSystem.Modules.Rating.Services.RatingService;
import EloRatingSystem.Modules.Rating.Services.SoloRatingService;
import EloRatingSystem.Modules.Stats.Services.MonthlyStatsService;
import EloRatingSystem.Modules.Stats.Services.SoloStatsService;
import EloRatingSystem.Modules.Stats.Services.StatsService;
import EloRatingSystem.Modules.Team.Models.Team;
import EloRatingSystem.Modules.Team.Repositories.TeamRepository;
import EloRatingSystem.Modules.Team.Services.TeamService;
import EloRatingSystem.Modules.player.Models.Player;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Service
public class MatchDeleteService {

    @Autowired
    MatchRepository matchRepository;
    @Autowired
    TeamRepository teamRepository;
    @Autowired
    RatingService ratingService;
    @Autowired
    TeamService teamService;
    @Autowired
    SoloRatingService soloRatingService;
    @Autowired
    MonthlyRatingService monthlyRatingService;
    @Autowired
    PlayerAchievementRepository playerAchievementRepository;
    @Autowired
    StatsService statsService;
    @Autowired
    MonthlyStatsService monthlyStatsService;
    @Autowired
    SoloMatchRepository soloMatchRepository;
    @Autowired
    SoloStatsService solostatsService;

    @Transactional
    public void deleteLatestMatch() {
        Match match = matchRepository.findTop1ByOrderByIdDesc().orElseThrow();
        deleteMatch(match);
    }

    @Transactional
    private void deleteMatch(Match match) {
        removeMatchStats(match);
        matchRepository.deleteById(match.getId());
    }

    @Transactional
    public void deleteLatestSoloMatch() {
        SoloMatch match = soloMatchRepository.findTop1ByOrderByIdDesc().orElseThrow();
        deleteMatch(match);
    }

    @Transactional
    public void deleteMatch(SoloMatch match) {
        soloRatingService.deleteRatingsBySoloMatch(match.getDate().toLocalDate(), match.getId());

        Player redPlayer = match.getRedPlayer();
        Player bluePlayer = match.getBluePlayer();

        playerAchievementRepository.deleteAllBySoloMatchId(match.getId());

        soloMatchRepository.deleteById(match.getId());
        solostatsService.deleteStreakByMatchId(match.getId());

        List<Player> players = new ArrayList<>(Arrays.asList(
                redPlayer,
                bluePlayer
        ));

        for (Player player : players) {
            solostatsService.undoPlayerStats(player, match, soloRatingService.getHighestELOByPlayerId(player.getId()), soloRatingService.getLowestELOByPlayerId(player.getId()));
        }
    }

    @Transactional
    public void deleteMatchById(long id) {

        List<Match> matches = matchRepository.findAllByIdGreaterThanEqual(id);

        if (matches.isEmpty()) {
            return;
        }

        matches.sort(Comparator.comparingLong(Match::getId));

        for (int i = matches.size() - 1; i >= 0; i--) {
            removeMatchStats(matches.get(i));
        }

        matchRepository.deleteById(matches.get(0).getId());
        matches.remove(0);

        for (Match m : matches) {
            Match match = matchRepository.save(m);
            ratingService.newRating(match);
            monthlyRatingService.newRating(match);

        }
    }

    @Transactional
    public Mono<TeamMatchResponseDto> updateMatchById(long id, TeamMatchRequestDto updatedMatchDto) {
        try {
            List<Match> matches = matchRepository.findAllByIdGreaterThanEqual(id);

            if (matches.isEmpty()) {
                return null;
            }

            Match updatedMatch = matches.get(0);

            Team red = teamService.getTeam(updatedMatchDto.getRedAtkId(), updatedMatchDto.getRedDefId());
            Team blue = teamService.getTeam(updatedMatchDto.getBlueAtkId(), updatedMatchDto.getBlueDefId());
            updatedMatch.setRedTeam(red);
            updatedMatch.setBlueTeam(blue);

            updatedMatch.setRedTeamScore(updatedMatchDto.getRedScore());
            updatedMatch.setBlueTeamScore(updatedMatchDto.getBlueScore());

            matches.sort(Comparator.comparingLong(Match::getId));

            for (int i = matches.size() - 1; i >= 0; i--) {
                removeMatchStats(matches.get(i));
            }

            for (Match m : matches) {
                Match match = matchRepository.save(m);
                ratingService.newRating(match);
                monthlyRatingService.newRating(match);
            }
            return Mono.just(new TeamMatchResponseDto(updatedMatch));
        } catch (ApiException e) {
            return Mono.error(e);
        }
    }

    private void removeMatchStats(Match match) {
        ratingService.deleteRatingsByMatch(match.getDate().toLocalDate(), match.getId());
        monthlyRatingService.deleteRatingsByMatch(match.getDate().toLocalDate(), match.getId());

        Team winner = match.getBlueTeamScore() < match.getRedTeamScore() ? match.getRedTeam() : match.getBlueTeam();
        Team loser = match.getBlueTeamScore() < match.getRedTeamScore() ? match.getBlueTeam() : match.getRedTeam();

        winner.setWon(winner.getWon() - 1);
        loser.setLost(loser.getLost() - 1);

        teamRepository.save(winner);
        teamRepository.save(loser);

        playerAchievementRepository.deleteAllByMatchId(match.getId());

        statsService.deleteStreakByMatchId(match.getId());
        monthlyStatsService.deleteStreakByMatchId(match.getId());

        List<Player> players = new ArrayList<>(Arrays.asList(
                winner.getAttacker(),
                winner.getDefender(),
                loser.getAttacker(),
                loser.getDefender()
        ));

        int month = match.getDate().toLocalDate().getMonthValue();
        int year = match.getDate().toLocalDate().getYear();

        for (Player player : players) {
            statsService.undoPlayerStats(player, match, ratingService.getHighestELOByPlayerId(player.getId()), ratingService.getLowestELOByPlayerId(player.getId()));
            monthlyStatsService.undoPlayerStats(player, match, monthlyRatingService.getHighestELOByPlayerId(player.getId(), month, year), monthlyRatingService.getLowestELOByPlayerId(player.getId(), month, year), month, year);
        }
    }


}
