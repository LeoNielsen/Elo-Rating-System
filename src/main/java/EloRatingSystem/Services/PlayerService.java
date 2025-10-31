package EloRatingSystem.Services;

import EloRatingSystem.Dtos.PlayerDtos.PlayerRequestDto;
import EloRatingSystem.Dtos.PlayerDtos.PlayerResponseDto;
import EloRatingSystem.Dtos.PlayerDtos.PlayerStatisticsResponseDto;
import EloRatingSystem.Dtos.PlayerDtos.SoloPlayerStatisticsResponseDto;
import EloRatingSystem.Dtos.RecordDto;
import EloRatingSystem.Dtos.RecordsDto;
import EloRatingSystem.Exception.ApiException;
import EloRatingSystem.Models.DailyStats.MonthlyDailyStats;
import EloRatingSystem.Models.DailyStats.PlayerDailyStats;
import EloRatingSystem.Models.DailyStats.SoloPlayerDailyStats;
import EloRatingSystem.Models.MonthlyStats;
import EloRatingSystem.Models.Player;
import EloRatingSystem.Models.PlayerStats;
import EloRatingSystem.Models.SoloPlayerStats;
import EloRatingSystem.Reporitories.DailyStats.MonthlyDailyStatsRepository;
import EloRatingSystem.Reporitories.DailyStats.PlayerDailyStatsRepository;
import EloRatingSystem.Reporitories.DailyStats.SoloPlayerDailyStatsRepository;
import EloRatingSystem.Reporitories.MonthlyStatsRepository;
import EloRatingSystem.Reporitories.PlayerRepository;
import EloRatingSystem.Reporitories.PlayerStatsRepository;
import EloRatingSystem.Reporitories.SoloPlayerStatsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

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
    MonthlyDailyStatsRepository monthlyDailyStatsRepository;
    @Autowired
    SoloPlayerStatsRepository soloPlayerStatsRepository;
    @Autowired
    SoloPlayerDailyStatsRepository soloPlayerDailyStatsRepository;
    @Autowired
    PlayerDailyStatsRepository playerDailyStatsRepository;


    public Mono<List<PlayerResponseDto>> getAllPlayers() {
        List<Player> players = playerRepository.findAll();
        List<PlayerResponseDto> playerResponseDtoList = new ArrayList<>();
        for (Player player : players) {
            if(!player.getActive()) {
                continue;
            }
            playerResponseDtoList.add(new PlayerResponseDto(player));
        }

        return Mono.just(playerResponseDtoList);
    }

    public Mono<PlayerResponseDto> getByNameTag(String nameTag) {
        Optional<Player> player = playerRepository.findByNameTagIgnoreCase(nameTag);
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
        return playerRepository.findByNameTagIgnoreCase(nameTag).isPresent();
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
            if(player.getActive()){
                playerStatistics.add(playerStatistics(player));
            }
        }

        return Mono.just(playerStatistics);
    }

    public PlayerStatisticsResponseDto playerStatistics(Player player) {
        return playerStatsRepository.findCombinedStatsByPlayerIdAndDate(player.getId(),LocalDate.now())
                .orElseGet(() -> new PlayerStatisticsResponseDto(player, 0));
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
        LocalDate date = LocalDate.now();
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
            if(player.getActive()){
                playerStatistics.add(soloPlayerStatistics(player));
            }
        }

        return Mono.just(playerStatistics);
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
        LocalDate date = LocalDate.now();
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


    public Mono<RecordsDto> getRecords() {
        List<PlayerStats> playerStatsList = playerStatsRepository.findAll(); // 2v2 stats
        List<SoloPlayerStats> soloPlayerStatsList = soloPlayerStatsRepository.findAll(); // 1v1 stats
        List<PlayerDailyStats> playerDailyStatsList = playerDailyStatsRepository.findAll();
        List<SoloPlayerDailyStats> soloPlayerDailyStatsList = soloPlayerDailyStatsRepository.findAll();

        RecordsDto records = new RecordsDto();
        

        // --- 2v2 Records ---
        records.setHighestRating2v2(playerStatsList.stream()
                .max(Comparator.comparingInt(PlayerStats::getHighestELO))
                .map(ps -> new RecordDto(ps.getPlayer().getNameTag(), ps.getHighestELO()))
                .orElse(null));

        records.setLowestRating2v2(playerStatsList.stream()
                .min(Comparator.comparingInt(PlayerStats::getLowestELO))
                .map(ps -> new RecordDto(ps.getPlayer().getNameTag(), ps.getLowestELO()))
                .orElse(null));

        records.setMostGames2v2(playerStatsList.stream()
                .max(Comparator.comparingInt(ps -> ps.getAttackerWins() + ps.getDefenderWins() + ps.getAttackerLost() + ps.getDefenderLost()))
                .map(ps -> new RecordDto(ps.getPlayer().getNameTag(), ps.getAttackerWins() + ps.getDefenderWins() + ps.getAttackerLost() + ps.getDefenderLost()))
                .orElse(null));

        records.setMostWins2v2(playerStatsList.stream()
                .max(Comparator.comparingInt(ps -> ps.getAttackerWins() + ps.getDefenderWins()))
                .map(ps -> new RecordDto(ps.getPlayer().getNameTag(), ps.getAttackerWins() + ps.getDefenderWins()))
                .orElse(null));

        records.setMostAttackerWins(playerStatsList.stream()
                .max(Comparator.comparingInt(PlayerStats::getAttackerWins))
                .map(ps -> new RecordDto(ps.getPlayer().getNameTag(), ps.getAttackerWins()))
                .orElse(null));

        records.setMostDefenderWins(playerStatsList.stream()
                .max(Comparator.comparingInt(PlayerStats::getDefenderWins))
                .map(ps -> new RecordDto(ps.getPlayer().getNameTag(), ps.getDefenderWins()))
                .orElse(null));

        records.setMostLost2v2(playerStatsList.stream()
                .max(Comparator.comparingInt(ps -> ps.getAttackerLost() + ps.getDefenderLost()))
                .map(ps -> new RecordDto(ps.getPlayer().getNameTag(), ps.getAttackerLost() + ps.getDefenderLost()))
                .orElse(null));

        records.setMostAttackerLost(playerStatsList.stream()
                .max(Comparator.comparingInt(PlayerStats::getAttackerLost))
                .map(ps -> new RecordDto(ps.getPlayer().getNameTag(), ps.getAttackerLost()))
                .orElse(null));

        records.setMostDefenderLost(playerStatsList.stream()
                .max(Comparator.comparingInt(PlayerStats::getDefenderLost))
                .map(ps -> new RecordDto(ps.getPlayer().getNameTag(), ps.getDefenderLost()))
                .orElse(null));

        records.setMostGoals2v2(playerStatsList.stream()
                .max(Comparator.comparingInt(PlayerStats::getGoals))
                .map(ps -> new RecordDto(ps.getPlayer().getNameTag(), ps.getGoals()))
                .orElse(null));

        records.setLongestWinStreak2v2(playerStatsList.stream()
                .max(Comparator.comparingInt(PlayerStats::getLongestWinStreak))
                .map(ps -> new RecordDto(ps.getPlayer().getNameTag(), ps.getLongestWinStreak()))
                .orElse(null));

        records.setHighestDailyEloChange2v2(playerDailyStatsList.stream()
                .max(Comparator.comparingInt(PlayerDailyStats::getRatingChange))
                .map(ps -> new RecordDto(ps.getPlayer().getNameTag(), ps.getRatingChange()))
                .orElse(null));

        records.setLowestDailyEloChange2v2(playerDailyStatsList.stream()
                .min(Comparator.comparingInt(PlayerDailyStats::getRatingChange))
                .map(ps -> new RecordDto(ps.getPlayer().getNameTag(), ps.getRatingChange()))
                .orElse(null));

        // --- 1v1 Records ---
        records.setHighestRating1v1(soloPlayerStatsList.stream()
                .max(Comparator.comparingInt(SoloPlayerStats::getHighestELO))
                .map(ps -> new RecordDto(ps.getPlayer().getNameTag(), ps.getHighestELO()))
                .orElse(null));

        records.setLowestRating1v1(soloPlayerStatsList.stream()
                .min(Comparator.comparingInt(SoloPlayerStats::getLowestELO))
                .map(ps -> new RecordDto(ps.getPlayer().getNameTag(), ps.getLowestELO()))
                .orElse(null));

        records.setMostGames1v1(soloPlayerStatsList.stream()
                .max(Comparator.comparingInt(ps -> ps.getWins() + ps.getLost()))
                .map(ps -> new RecordDto(ps.getPlayer().getNameTag(), ps.getWins() + ps.getLost()))
                .orElse(null));

        records.setMostWins1v1(soloPlayerStatsList.stream()
                .max(Comparator.comparingInt(SoloPlayerStats::getWins))
                .map(ps -> new RecordDto(ps.getPlayer().getNameTag(), ps.getWins()))
                .orElse(null));

        records.setMostLost1v1(soloPlayerStatsList.stream()
                .max(Comparator.comparingInt(SoloPlayerStats::getLost))
                .map(ps -> new RecordDto(ps.getPlayer().getNameTag(), ps.getLost()))
                .orElse(null));

        records.setMostGoals1v1(soloPlayerStatsList.stream()
                .max(Comparator.comparingInt(SoloPlayerStats::getGoals))
                .map(ps -> new RecordDto(ps.getPlayer().getNameTag(), ps.getGoals()))
                .orElse(null));

        records.setLongestWinStreak1v1(soloPlayerStatsList.stream()
                .max(Comparator.comparingInt(SoloPlayerStats::getLongestWinStreak))
                .map(ps -> new RecordDto(ps.getPlayer().getNameTag(), ps.getLongestWinStreak()))
                .orElse(null));

        records.setHighestDailyEloChange1v1(soloPlayerDailyStatsList.stream()
                .max(Comparator.comparingInt(SoloPlayerDailyStats::getRatingChange))
                .map(ps -> new RecordDto(ps.getPlayer().getNameTag(), ps.getRatingChange()))
                .orElse(null));

        records.setLowestDailyEloChange1v1(soloPlayerDailyStatsList.stream()
                .min(Comparator.comparingInt(SoloPlayerDailyStats::getRatingChange))
                .map(ps -> new RecordDto(ps.getPlayer().getNameTag(), ps.getRatingChange()))
                .orElse(null));

        return Mono.just(records);
    }

}
