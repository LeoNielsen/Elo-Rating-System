package EloRatingSystem.Services;

import EloRatingSystem.Dtos.PlayerRequestDto;
import EloRatingSystem.Dtos.PlayerResponseDto;
import EloRatingSystem.Dtos.PlayerStatisticsResponseDto;
import EloRatingSystem.Dtos.SoloPlayerStatisticsResponseDto;
import EloRatingSystem.Exception.ApiException;
import EloRatingSystem.Models.DailyStats.MonthlyDailyStats;
import EloRatingSystem.Models.DailyStats.PlayerDailyStats;
import EloRatingSystem.Models.DailyStats.SoloPlayerDailyStats;
import EloRatingSystem.Models.*;
import EloRatingSystem.Reporitories.DailyStats.MonthlyDailyStatsRepository;
import EloRatingSystem.Reporitories.DailyStats.PlayerDailyStatsRepository;
import EloRatingSystem.Reporitories.DailyStats.SoloPlayerDailyStatsRepository;
import EloRatingSystem.Reporitories.*;
import EloRatingSystem.Services.RatingServices.MonthlyRatingService;
import EloRatingSystem.Services.RatingServices.RatingService;
import EloRatingSystem.Services.RatingServices.SoloRatingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class PlayerService {

    @Autowired
    PlayerRepository playerRepository;
    @Autowired
    PlayerStatsRepository playerStatsRepository;
    @Autowired
    MonthlyStatsRepository monthlyStatsRepository;
    @Autowired
    PlayerDailyStatsRepository playerDailyStatsRepository;
    @Autowired
    MonthlyDailyStatsRepository monthlyDailyStatsRepository;
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
    RatingService ratingService;

    @Autowired
    SoloRatingService soloRatingService;
    @Autowired
    MonthlyRatingRepository monthlyRatingRepository;
    @Autowired
    MonthlyRatingService monthlyRatingService;
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
                    if (match.getRedTeam().getAttacker().equals(player)) {
                        attackerWins++;
                    } else {
                        defenderWins++;
                    }
                    currentWinStreak++;
                    if (currentWinStreak > longestWinStreak) {
                        longestWinStreak = currentWinStreak;
                    }
                } else {
                    if (match.getRedTeam().getAttacker().equals(player)) {
                        attackerLost++;
                    } else {
                        defenderLost++;
                    }
                    currentWinStreak = 0;
                }
                goals += match.getRedTeamScore();
            } else {
                if (match.getBlueTeamScore() == 10) {
                    if (match.getBlueTeam().getAttacker().equals(player)) {
                        attackerWins++;
                    } else {
                        defenderWins++;
                    }
                    currentWinStreak++;
                    if (currentWinStreak > longestWinStreak) {
                        longestWinStreak = currentWinStreak;
                    }
                } else {
                    if (match.getBlueTeam().getAttacker().equals(player)) {
                        attackerLost++;
                    } else {
                        defenderLost++;
                    }
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
        regenerateDailyStats(player);
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
            return new SoloPlayerStatisticsResponseDto(player, todayRatingChance);
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

    private void regenerateSoloDailyStats(Player player) {
        Date date = new Date(System.currentTimeMillis());
        SoloPlayerDailyStats stats = soloPlayerDailyStatsRepository.findAllByPlayerIdAndDate(player.getId(), date).orElseGet(() -> new SoloPlayerDailyStats(player, date, 0));
        stats.setRatingChange(0);
        soloPlayerDailyStatsRepository.save(stats);

        LocalDate today = LocalDate.now();
        List<SoloMatch> matches = soloMatchRepository.findAllByDateAndRedPlayerIdOrDateAndBluePlayerId(today, player.getId(), today, player.getId());
        for (SoloMatch match : matches) {
            SoloPlayerRating rating = soloRatingRepository.findBySoloMatchIdAndPlayerId(match.getId(), player.getId()).orElseThrow();
            soloRatingService.updatePlayerDailyStats(rating.getNewRating() - rating.getOldRating(), player);
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
        regenerateSoloDailyStats(player);
        Optional<SoloPlayerStats> playerStats = soloPlayerStatsRepository.findByPlayerId(player.getId());
        if (playerStats.isPresent()) {
            soloPlayerStatsRepository.save(new SoloPlayerStats(playerStats.get().getId(), player, wins, lost, goals, highestELO, lowestELO, longestWinStreak, currentWinStreak));
        } else {
            soloPlayerStatsRepository.save(new SoloPlayerStats(player, wins, lost, goals, highestELO, lowestELO, longestWinStreak, currentWinStreak));
        }
    }

    public Mono<List<PlayerStatisticsResponseDto>> getAllMonthlyStatistics() {
        LocalDate today = LocalDate.now();
        int month = today.getMonthValue();
        int year = today.getYear();

        List<PlayerStatisticsResponseDto> playerStatistics = new ArrayList<>();

        List<Player> players = playerRepository.findAll();
        for (Player player : players) {
            playerStatistics.add(monthlyStatistics(player, month, year));
        }

        return Mono.just(playerStatistics);
    }

    public Mono<PlayerStatisticsResponseDto> getMonthlyStatistics(Long id) {
        LocalDate today = LocalDate.now();
        int month = today.getMonthValue();
        int year = today.getYear();
        Optional<Player> playerOptional = playerRepository.findById(id);
        if (playerOptional.isPresent()) {
            Player player = playerOptional.get();
            return Mono.just(monthlyStatistics(player, month, year));
        } else {
            return Mono.error(new ApiException(String.format("%s Doesn't exist", id), HttpStatus.BAD_REQUEST));
        }
    }

    public PlayerStatisticsResponseDto monthlyStatistics(Player player, int month, int year) {
        int todayRatingChance = 0;
        Date date = new Date(System.currentTimeMillis());
        Optional<MonthlyDailyStats> playerDailyStats = monthlyDailyStatsRepository.findAllByPlayerIdAndDate(player.getId(), date);
        if (playerDailyStats.isPresent()) {
            todayRatingChance = playerDailyStats.get().getRatingChange();
        }
        Optional<MonthlyStats> playerStatsOptional = monthlyStatsRepository.findByPlayerIdAndMonthAndYear(player.getId(), month, year);
        if (playerStatsOptional.isPresent()) {
            return new PlayerStatisticsResponseDto(player, playerStatsOptional.get(), todayRatingChance);
        } else {
            return new PlayerStatisticsResponseDto(player, todayRatingChance);
        }
    }

    public void monthlyStatisticsGenAll() {
        LocalDate today = LocalDate.now();
        int month = today.getMonthValue();
        int year = today.getYear();

        List<Player> players = playerRepository.findAll();
        for (Player player : players) {
            regenerateMonthlyStatistics(player);
        }
        List<Match> matches = matchRepository.findAll();
        for (Match match : matches) {
            LocalDate date = match.getDate().toLocalDate();
            if (date.getMonthValue() == month && date.getYear() == year) {
                monthlyRatingService.newRating(match);
            }
        }
        for (Player player : players) {
            regenerateMonthlyDailyStats(player);
        }
    }

    private void regenerateMonthlyDailyStats(Player player) {
        Date date = new Date(System.currentTimeMillis());
        MonthlyDailyStats stats = monthlyDailyStatsRepository.findAllByPlayerIdAndDate(player.getId(), date).orElseGet(() -> new MonthlyDailyStats(player, date, 0));
        stats.setRatingChange(0);
        monthlyDailyStatsRepository.save(stats);

        LocalDate today = LocalDate.now();
        List<Team> teams = teamRepository.findAllByAttackerIdOrDefenderId(player.getId(), player.getId());
        List<Match> matches = new ArrayList<>();
        for (Team team : teams) {
            matches.addAll(matchRepository.findAllByDateAndRedTeamIdOrDateAndBlueTeamId(today, team.getId(), today, team.getId()));
        }
        for (Match match : matches) {
            MonthlyRating rating = monthlyRatingRepository.findByMatchIdAndPlayerId(match.getId(), player.getId()).orElseThrow();
            monthlyRatingService.updateMonthlyDailyStats(rating.getNewRating() - rating.getOldRating(), player);
        }
    }

    private void regenerateDailyStats(Player player) {
        Date date = new Date(System.currentTimeMillis());
        PlayerDailyStats stats = playerDailyStatsRepository.findAllByPlayerIdAndDate(player.getId(), date).orElseGet(() -> new PlayerDailyStats(player, date, 0));
        stats.setRatingChange(0);
        playerDailyStatsRepository.save(stats);

        LocalDate today = LocalDate.now();
        List<Team> teams = teamRepository.findAllByAttackerIdOrDefenderId(player.getId(), player.getId());
        List<Match> matches = new ArrayList<>();
        for (Team team : teams) {
            matches.addAll(matchRepository.findAllByDateAndRedTeamIdOrDateAndBlueTeamId(today, team.getId(), today, team.getId()));
        }
        for (Match match : matches) {
            PlayerRating rating = ratingRepository.findByMatchIdAndPlayerId(match.getId(), player.getId()).orElseThrow();
            ratingService.updatePlayerDailyStats(rating.getNewRating() - rating.getOldRating(), player);
        }
    }

    public void regenerateMonthlyStatistics(Player player) {
        LocalDate today = LocalDate.now();
        int month = today.getMonthValue();
        int year = today.getYear();

        List<Team> teams = teamRepository.findAllByAttackerIdOrDefenderId(player.getId(), player.getId());
        List<Match> matches = new ArrayList<>();
        for (Team team : teams) {
            matches.addAll(matchRepository.findAllByRedTeamIdOrBlueTeamId(team.getId(), team.getId()));
        }
        matches.sort(Comparator.comparingLong(Match::getId));

        for (Match match : matches) {
            LocalDate date = match.getDate().toLocalDate();
            if (date.getMonthValue() == month && date.getYear() == year) {
                Optional<MonthlyRating> opt = monthlyRatingRepository.findByMatchIdAndPlayerId(match.getId(),player.getId());
                opt.ifPresent(monthlyRating -> monthlyRatingRepository.delete(monthlyRating));
                Optional<MonthlyStats> statsOpt = monthlyStatsRepository.findByPlayerIdAndMonthAndYear(player.getId(), month,year);
                statsOpt.ifPresent(stats -> monthlyStatsRepository.delete(stats));
            }
        }
    }

}
