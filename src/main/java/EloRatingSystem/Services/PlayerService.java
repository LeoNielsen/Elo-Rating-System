package EloRatingSystem.Services;

import EloRatingSystem.Dtos.PlayerRequestDto;
import EloRatingSystem.Dtos.PlayerResponseDto;
import EloRatingSystem.Dtos.PlayerStatisticsResponseDto;
import EloRatingSystem.Dtos.SoloPlayerStatisticsResponseDto;
import EloRatingSystem.Exception.ApiException;
import EloRatingSystem.Models.*;
import EloRatingSystem.Reporitories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.sql.Date;
import java.util.*;

@Service
public class PlayerService {

    @Autowired
    PlayerRepository playerRepository;
    @Autowired
    PlayerStatsRepository playerStatsRepository;
    @Autowired
    PlayerDailyStatsRepository playerDailyStatsRepository;
    @Autowired
    SoloPlayerStatsRepository soloPlayerStatsRepository;
    @Autowired
    SoloPlayerDailyStatsRepository soloPlayerDailyStatsRepository;
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

    public Mono<List<PlayerResponseDto>> getAllPlayers() {
        List<Player> players = playerRepository.findAll();
        List<PlayerResponseDto> playerResponseDtoList = new ArrayList<>();
        for (Player player : players) {
            playerResponseDtoList.add(new PlayerResponseDto(player));
        }

        return Mono.just(playerResponseDtoList);
    }

    public Mono<PlayerResponseDto> getByNameTag(String nameTag) {
        Optional<Player> player = playerRepository.findByNameTag(nameTag);
        if (player.isPresent()) {
            PlayerResponseDto responseDto = new PlayerResponseDto(player.get());
            return Mono.just(responseDto);
        }
        return Mono.error(new ApiException(String.format("%s Doesn't exist", nameTag), HttpStatus.BAD_REQUEST));
    }

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
        int todayRatingChance = 0;
        Date date = new Date(System.currentTimeMillis());
        Optional<PlayerDailyStats> playerDailyStats = playerDailyStatsRepository.findAllByPlayerIdAndDate(player.getId(), date);
        if (playerDailyStats.isPresent()) {
            todayRatingChance = playerDailyStats.get().getRatingChange();
        }
        Optional<PlayerStats> playerStatsOptional = playerStatsRepository.findByPlayerId(player.getId());
        if (playerStatsOptional.isPresent()) {
            return new PlayerStatisticsResponseDto(player, playerStatsOptional.get(), todayRatingChance);
        } else {
            return new PlayerStatisticsResponseDto(player, todayRatingChance);
        }
    }

    public void regeneratePlayerStatistics(Player player) {
        List<Team> teams = teamRepository.findAllByAttackerIdOrDefenderId(player.getId(), player.getId());

        int attackerWins = 0;
        int defenderWins = 0;
        int attackerLost = 0;
        int defenderLost = 0;
        int goals = 0;
        int highestELO = 1200;
        int lowestELO = 1200;
        int longestWinStreak = 0;
        int currentWinStreak = 0;

        List<Match> matches = new ArrayList<>();

        for (Team team : teams) {
            if (team.getAttacker().getId().equals(player.getId())) {
                attackerWins += team.getWon();
                attackerLost += team.getLost();
            } else {
                defenderWins += team.getWon();
                defenderLost += team.getLost();
            }
            matches.addAll(matchRepository.findAllByRedTeamIdOrBlueTeamId(team.getId(), team.getId()));
        }

        if (!matches.isEmpty()) {
            highestELO = ratingRepository.findTopByPlayerIdOrderByNewRatingDesc(player.getId()).orElseThrow().getNewRating();
            lowestELO = ratingRepository.findTopByPlayerIdOrderByNewRatingAsc(player.getId()).orElseThrow().getNewRating();
        }

        matches.sort(Comparator.comparingLong(Match::getId));

        for (Match match : matches) {
            if (match.getRedTeam().getAttacker().getId().equals(player.getId()) || match.getRedTeam().getDefender().getId().equals(player.getId())) {
                if (match.getRedTeamScore() == 10) {
                    currentWinStreak++;
                    if (currentWinStreak > longestWinStreak) {
                        longestWinStreak = currentWinStreak;
                    }
                } else {
                    currentWinStreak = 0;
                }
                goals += match.getRedTeamScore();
            } else {
                if (match.getBlueTeamScore() == 10) {
                    currentWinStreak++;
                    if (currentWinStreak > longestWinStreak) {
                        longestWinStreak = currentWinStreak;
                    }
                } else {
                    currentWinStreak = 0;
                }
                goals += match.getBlueTeamScore();
            }
        }
        Optional<PlayerStats> playerStats = playerStatsRepository.findByPlayerId(player.getId());
        if (playerStats.isPresent()) {
            playerStatsRepository.save(new PlayerStats(playerStats.get().getId(), player, attackerWins, defenderWins, attackerLost, defenderLost, goals, highestELO, lowestELO, longestWinStreak, currentWinStreak));
        } else {
            playerStatsRepository.save(new PlayerStats(player, attackerWins, defenderWins, attackerLost, defenderLost, goals, highestELO, lowestELO, longestWinStreak, currentWinStreak));
        }
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
        int todayRatingChance = 0;
        Date date = new Date(System.currentTimeMillis());
        Optional<SoloPlayerDailyStats> playerDailyStats = soloPlayerDailyStatsRepository.findAllByPlayerIdAndDate(player.getId(), date);
        if (playerDailyStats.isPresent()) {
            todayRatingChance = playerDailyStats.get().getRatingChange();
        }
        Optional<SoloPlayerStats> playerStatsOptional = soloPlayerStatsRepository.findByPlayerId(player.getId());
        if (playerStatsOptional.isPresent()) {
            return new SoloPlayerStatisticsResponseDto(player, playerStatsOptional.get(), todayRatingChance);
        } else {
            return new SoloPlayerStatisticsResponseDto(player,todayRatingChance);
        }
    }

    public Mono<List<SoloPlayerStatisticsResponseDto>> getAllSoloStatistics() {
        List<SoloPlayerStatisticsResponseDto> playerStatistics = new ArrayList<>();

        List<Player> players = playerRepository.findAll();
        for (Player player : players) {
                playerStatistics.add(soloPlayerStatistics(player));
        }

        return Mono.just(playerStatistics);
    }


    public void playerStatisticsGenAll() {
        List<Player> players = playerRepository.findAll();
        for (Player player : players) {
            regeneratePlayerStatistics(player);
        }
    }

    public void regenerateSoloPlayerStatisticsAll() {
        List<Player> players = playerRepository.findAll();
        for (Player player : players) {
            regenerateSoloPlayerStatistics(player);
        }
    }

    public void regenerateSoloPlayerStatistics(Player player) {
        int wins = 0;
        int lost = 0;
        int goals = 0;
        int highestELO = 1200;
        int lowestELO = 1200;
        int longestWinStreak = 0;
        int currentWinStreak = 0;

        List<SoloMatch> matches = soloMatchRepository.findAllByRedPlayerIdOrBluePlayerId(player.getId(), player.getId());
        if (!matches.isEmpty()) {
            highestELO = soloRatingRepository.findTopMaxNewRatingByPlayerId(player.getId()).orElseThrow().getNewRating();
            lowestELO = soloRatingRepository.findTopMinNewRatingByPlayerId(player.getId()).orElseThrow().getNewRating();
        }

        matches.sort(Comparator.comparingLong(SoloMatch::getId));

        for (SoloMatch match : matches) {
            if (match.getRedPlayer().getId().equals(player.getId())) {
                goals += match.getRedScore();
                if (match.getRedScore() == 10) {
                    wins += 1;
                    currentWinStreak++;
                    if (currentWinStreak > longestWinStreak) {
                        longestWinStreak = currentWinStreak;
                    }
                } else {
                    lost += 1;
                    currentWinStreak = 0;
                }
            } else {
                goals += match.getBlueScore();
                if (match.getBlueScore() == 10) {
                    wins += 1;
                    currentWinStreak++;
                    if (currentWinStreak > longestWinStreak) {
                        longestWinStreak = currentWinStreak;
                    }
                } else {
                    lost += 1;
                    currentWinStreak = 0;
                }
            }

        }

        Optional<SoloPlayerStats> playerStats = soloPlayerStatsRepository.findByPlayerId(player.getId());
        if (playerStats.isPresent()) {
            soloPlayerStatsRepository.save(new SoloPlayerStats(playerStats.get().getId(), player, wins, lost, goals, highestELO, lowestELO, longestWinStreak, currentWinStreak));
        } else {
            soloPlayerStatsRepository.save(new SoloPlayerStats(player, wins, lost, goals, highestELO, lowestELO, longestWinStreak, currentWinStreak));
        }
    }
}
