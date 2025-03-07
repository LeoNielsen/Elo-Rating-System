package EloRatingSystem.Services;

import EloRatingSystem.Dtos.*;
import EloRatingSystem.Exception.ApiException;
import EloRatingSystem.Models.*;
import EloRatingSystem.Reporitories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PlayerService {

    @Autowired
    PlayerRepository playerRepository;
    @Autowired
    MatchRepository matchRepository;
    @Autowired
    SoloMatchRepository soloMatchRepository;
    @Autowired
    TeamRepository teamRepository;
    @Autowired
    RatingRepository ratingRepository;
    @Autowired
    SoloRatingRepository soloRatingRepository;

    public Mono<PlayerResponseDto> newPlayer(PlayerRequestDto requestDto) {
        if (!checkIfPlayerExists(requestDto.getNameTag())) {
            Player player = new Player(requestDto.getNameTag(), 1200, 1200, true);
            playerRepository.save(player);
            return Mono.just(new PlayerResponseDto(player));
        }
        return Mono.error(new ApiException(String.format("NameTag %s is already in use", requestDto.getNameTag()), HttpStatus.INTERNAL_SERVER_ERROR));
    }

    public boolean checkIfPlayerExists(String nameTag) {
        return playerRepository.findByNameTag(nameTag).isPresent();
    }

    public Mono<PlayerResponseDto> getByNameTag(String nameTag) {
        Optional<Player> player = playerRepository.findByNameTag(nameTag);
        if (player.isPresent()) {
            PlayerResponseDto responseDto = new PlayerResponseDto(player.get());
            return Mono.just(responseDto);
        }
        return Mono.error(new ApiException(String.format("%s Doesn't exist", nameTag), HttpStatus.BAD_REQUEST));
    }

    public Mono<List<PlayerResponseDto>> getAllPlayers() {
        List<Player> players = playerRepository.findAll();
        List<PlayerResponseDto> playerResponseDtoList = new ArrayList<>();
        for (Player player : players) {
            playerResponseDtoList.add(new PlayerResponseDto(player));
        }

        return Mono.just(playerResponseDtoList);
    }

    public Mono<PlayerStatisticsResponseDto> getStatistics(Long id) {
        Optional<Player> playerOptional = playerRepository.findById(id);
        if (playerOptional.isPresent()) {
            Player player = playerOptional.get();
            return Mono.just(playerStatistics(player));
        } else {
            return Mono.error(new ApiException(String.format("%s Doesn't exist", id), HttpStatus.BAD_REQUEST));
        }
    }

    public Mono<List<PlayerStatisticsResponseDto>> getAllStatistics() {
        List<PlayerStatisticsResponseDto> playerStatistics = new ArrayList<>();

        List<Player> players = playerRepository.findAll();
        for (Player player : players) {
            playerStatistics.add(playerStatistics(player));
        }

        return Mono.just(playerStatistics);
    }

    public PlayerStatisticsResponseDto playerStatistics(Player player) {
        List<Team> teams = teamRepository.findAllByAttackerIdOrDefenderId(player.getId(), player.getId());

        int todayRatingChance = 0;
        int attackerWins = 0;
        int defenderWins = 0;
        int attackerLost = 0;
        int defenderLost = 0;
        int goals = 0;
        Date date = new Date(System.currentTimeMillis());
        for (Team team : teams) {
            if (team.getAttacker().getId().equals(player.getId())) {
                attackerWins += team.getWon();
                attackerLost += team.getLost();
            } else {
                defenderWins += team.getWon();
                defenderLost += team.getLost();
            }

            List<Match> matches = matchRepository.findAllByRedTeamIdOrBlueTeamId(team.getId(), team.getId());
            for (Match match : matches) {
                if (match.getRedTeam().getAttacker().getId().equals(player.getId()) || match.getRedTeam().getDefender().getId().equals(player.getId())) {
                    goals += match.getRedTeamScore();
                } else {
                    goals += match.getBlueTeamScore();
                }
                if (match.getDate().toString().equals(date.toString())) {
                    List<PlayerRating> ratings = ratingRepository.findAllByMatchId(match.getId());
                    for (PlayerRating rating : ratings) {
                        if (rating.getPlayer().getId().equals(player.getId())) {
                            todayRatingChance += rating.getNewRating() - rating.getOldRating();
                        }
                    }
                }
            }
        }

        return new PlayerStatisticsResponseDto(player.getId(), player.getNameTag(), player.getRating(), attackerWins, defenderWins, attackerLost, defenderLost, goals, todayRatingChance);
    }


    public Mono<SoloPlayerStatisticsResponseDto> getSoloStatistics(Long id) {
        Optional<Player> playerOptional = playerRepository.findById(id);
        if (playerOptional.isPresent()) {
            Player player = playerOptional.get();
            return Mono.just(soloPlayerStatistics(player));
        } else {
            return Mono.error(new ApiException(String.format("%s Doesn't exist", id), HttpStatus.BAD_REQUEST));
        }
    }

    private SoloPlayerStatisticsResponseDto soloPlayerStatistics(Player player) {
        int wins = 0;
        int lost = 0;
        int goals = 0;
        int todayRatingChance = 0;

        Date date = new Date(System.currentTimeMillis());
        List<SoloMatch> matches = soloMatchRepository.findAllByRedPlayerIdOrBluePlayerId(player.getId(), player.getId());
        for (SoloMatch match : matches) {
            if (match.getRedPlayer().getId().equals(player.getId())) {
                goals += match.getRedScore();
                if (match.getRedScore() == 10) {
                    wins += 1;
                } else {
                    lost += 1;
                }
            } else {
                goals += match.getBlueScore();
                if (match.getBlueScore() == 10) {
                    wins += 1;
                } else {
                    lost += 1;
                }
            }
            if (match.getDate().toString().equals(date.toString())) {
                List<SoloPlayerRating> ratings = soloRatingRepository.findAllBySoloMatchId(match.getId());
                for (SoloPlayerRating rating : ratings) {
                    if (rating.getPlayer().getId().equals(player.getId())) {
                        todayRatingChance += rating.getNewRating() - rating.getOldRating();
                    }
                }
            }
        }
        return new SoloPlayerStatisticsResponseDto(player.getId(), player.getNameTag(), player.getSoloRating(), wins, lost, goals,todayRatingChance);

    }

    public Mono<List<SoloPlayerStatisticsResponseDto>> getAllSoloStatistics() {
        List<SoloPlayerStatisticsResponseDto> playerStatistics = new ArrayList<>();

        List<Player> players = playerRepository.findAll();
        for (Player player : players) {
            playerStatistics.add(soloPlayerStatistics(player));
        }

        return Mono.just(playerStatistics);
    }
}
