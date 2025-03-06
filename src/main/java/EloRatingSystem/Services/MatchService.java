package EloRatingSystem.Services;

import EloRatingSystem.Dtos.MatchRequestDto;
import EloRatingSystem.Dtos.MatchResponseDto;
import EloRatingSystem.Dtos.SoloMatchRequestDto;
import EloRatingSystem.Dtos.SoloMatchResponseDto;
import EloRatingSystem.Exception.ApiException;
import EloRatingSystem.Models.Match;
import EloRatingSystem.Models.Player;
import EloRatingSystem.Models.SoloMatch;
import EloRatingSystem.Models.Team;
import EloRatingSystem.Reporitories.MatchRepository;
import EloRatingSystem.Reporitories.PlayerRepository;
import EloRatingSystem.Reporitories.SoloMatchRepository;
import EloRatingSystem.Reporitories.TeamRepository;
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
    SoloMatchRepository soloMatchRepository;
    @Autowired
    TeamRepository teamRepository;
    @Autowired
    PlayerRepository playerRepository;
    @Autowired
    RatingService ratingService;

    public Mono<List<MatchResponseDto>> getAllMatches() {
        List<Match> matches = matchRepository.findAll();
        List<MatchResponseDto> matchResponseDtoList = new ArrayList<>();
        for (Match match : matches) {
            matchResponseDtoList.add(new MatchResponseDto(match));
        }

        return Mono.just(matchResponseDtoList);
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

            match = matchRepository.save(match);

            return Mono.just(new MatchResponseDto(match));
        }

        return Mono.error(new ApiException(
                String.format("Either team %s or %s does not exits", requestDto.getRedTeamId(), requestDto.getBlueTeamId())
                , HttpStatus.BAD_REQUEST));
    }

    public Mono<MatchResponseDto> getMatchById(Long id) {
        Optional<Match> match = matchRepository.findById(id);
        return match.map(value -> Mono.just(new MatchResponseDto(value)))
                .orElseGet(() -> Mono.error(new ApiException(String.format("match %s doesn't exist", id), HttpStatus.BAD_REQUEST)));
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

        matchRepository.deleteById(match.getId());
    }

    public Mono<SoloMatchResponseDto> getSoloMatchById(Long id) {
        Optional<SoloMatch> match = soloMatchRepository.findById(id);
        return match.map(value -> Mono.just(new SoloMatchResponseDto(value)))
                .orElseGet(() -> Mono.error(new ApiException(String.format("match %s doesn't exist", id), HttpStatus.BAD_REQUEST)));

    }

    public Mono<List<SoloMatchResponseDto>> getAllSoloMatches() {
        List<SoloMatch> matches = soloMatchRepository.findAll();
        List<SoloMatchResponseDto> matchResponseDtoList = new ArrayList<>();
        for (SoloMatch match : matches) {
            matchResponseDtoList.add(new SoloMatchResponseDto(match));
        }

        return Mono.just(matchResponseDtoList);
    }

    public Mono<SoloMatchResponseDto> newSoloMatch(SoloMatchRequestDto requestDto) {
        Optional<Player> redPlayerOptional = playerRepository.findById(requestDto.getRedPlayerId());
        Optional<Player> bluePlayerOptional = playerRepository.findById(requestDto.getBluePlayerId());

        if (bluePlayerOptional.isPresent() && redPlayerOptional.isPresent()) {
            Player redPlayer = redPlayerOptional.get();
            Player bluePlayer = bluePlayerOptional.get();

            SoloMatch match = soloMatchRepository.save(new SoloMatch(new Date(System.currentTimeMillis()), redPlayer, bluePlayer,
                    requestDto.getRedScore(), requestDto.getBlueScore()));

            match = ratingService.newSoloRating(match);

            match = soloMatchRepository.save(match);

            return Mono.just(new SoloMatchResponseDto(match));
        }

        return Mono.error(new ApiException(
                String.format("Either player %s or %s does not exits", requestDto.getRedPlayerId(), requestDto.getBluePlayerId())
                , HttpStatus.BAD_REQUEST));
    }

    public void deleteLatestSoloMatch() {
        SoloMatch match = soloMatchRepository.findTop1ByOrderByIdDesc().orElseThrow();
        ratingService.deleteRatingsBySoloMatch(match.getId());

        soloMatchRepository.deleteById(match.getId());
    }
}
