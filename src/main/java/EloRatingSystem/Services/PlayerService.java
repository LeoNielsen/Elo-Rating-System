package EloRatingSystem.Services;

import EloRatingSystem.Dtos.PlayerDtos.PlayerRequestDto;
import EloRatingSystem.Dtos.PlayerDtos.PlayerResponseDto;
import EloRatingSystem.Dtos.PlayerDtos.PlayerStatisticsResponseDto;
import EloRatingSystem.Dtos.PlayerDtos.SoloPlayerStatisticsResponseDto;
import EloRatingSystem.Exception.ApiException;
import EloRatingSystem.Models.DailyStats.MonthlyDailyStats;
import EloRatingSystem.Models.DailyStats.SoloPlayerDailyStats;
import EloRatingSystem.Models.MonthlyStats;
import EloRatingSystem.Models.Player;
import EloRatingSystem.Models.SoloPlayerStats;
import EloRatingSystem.Reporitories.DailyStats.MonthlyDailyStatsRepository;
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
            if(!player.getActive()){
                continue;
            }
            playerStatistics.add(playerStatistics(player));
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
            playerStatistics.add(soloPlayerStatistics(player));
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









}
