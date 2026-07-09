package EloRatingSystem.Modules.Matches.Services;

import EloRatingSystem.Exception.ApiException;
import EloRatingSystem.Modules.Matches.Dtos.SoloMatchRequestDto;
import EloRatingSystem.Modules.Matches.Dtos.SoloMatchResponseDto;
import EloRatingSystem.Modules.Matches.Dtos.TeamMatchRequestDto;
import EloRatingSystem.Modules.Matches.Dtos.TeamMatchResponseDto;
import EloRatingSystem.Modules.Matches.Models.Match;
import EloRatingSystem.Modules.Matches.Models.SoloMatch;
import EloRatingSystem.Modules.Matches.Repositories.MatchRepository;
import EloRatingSystem.Modules.Matches.Repositories.SoloMatchRepository;
import EloRatingSystem.Modules.Matches.Utils.MatchUtils;
import EloRatingSystem.Modules.Rating.Services.MonthlyRatingService;
import EloRatingSystem.Modules.Rating.Services.RatingService;
import EloRatingSystem.Modules.Rating.Services.SoloRatingService;
import EloRatingSystem.Modules.Rating.Services.TeamRatingService;
import EloRatingSystem.Modules.Team.Models.Team;
import EloRatingSystem.Modules.Team.Services.TeamService;
import EloRatingSystem.Modules.player.Models.Player;
import EloRatingSystem.Modules.player.Services.PlayerService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.List;

@Service
public class MatchUpdateService {

    @Autowired
    MatchRepository matchRepository;
    @Autowired
    SoloMatchRepository soloMatchRepository;
    @Autowired
    MonthlyRatingService monthlyRatingService;
    @Autowired
    RatingService ratingService;
    @Autowired
    TeamRatingService teamRatingService;
    @Autowired
    SoloRatingService soloRatingService;
    @Autowired
    TeamService teamService;
    @Autowired
    PlayerService playerService;
    @Autowired
    MatchUtils matchUtils;


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
                matchUtils.removeMatchStats(matches.get(i));
            }

            for (Match m : matches) {
                Match match = matchRepository.save(m);
                ratingService.newRating(match);
                teamRatingService.newTeamRating(match);
                monthlyRatingService.newRating(match);
            }
            return Mono.just(new TeamMatchResponseDto(updatedMatch));
        } catch (ApiException e) {
            return Mono.error(e);
        }
    }

    @Transactional
    public Mono<SoloMatchResponseDto> updateSoloMatchById(long id, SoloMatchRequestDto updatedMatchDto) {
        try {
            List<SoloMatch> matches = soloMatchRepository.findAllByIdGreaterThanEqual(id);

            if (matches.isEmpty()) {
                return null;
            }

            SoloMatch updatedMatch = matches.get(0);

            Player red = playerService.getById(updatedMatchDto.getRedPlayerId());
            Player blue = playerService.getById(updatedMatchDto.getBluePlayerId());
            updatedMatch.setRedPlayer(red);
            updatedMatch.setBluePlayer(blue);

            updatedMatch.setRedScore(updatedMatchDto.getRedScore());
            updatedMatch.setBlueScore(updatedMatchDto.getBlueScore());

            matches.sort(Comparator.comparingLong(SoloMatch::getId));

            for (int i = matches.size() - 1; i >= 0; i--) {
                matchUtils.removeMatchStats(matches.get(i));
            }

            for (SoloMatch m : matches) {
                SoloMatch match = soloMatchRepository.save(m);
                soloRatingService.newSoloRating(match);
            }
            return Mono.just(new SoloMatchResponseDto(updatedMatch));
        } catch (ApiException e) {
            return Mono.error(e);
        }
    }
}
