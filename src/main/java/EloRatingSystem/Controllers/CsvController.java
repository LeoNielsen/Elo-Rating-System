package EloRatingSystem.Controllers;

import EloRatingSystem.Dtos.*;
import EloRatingSystem.Dtos.MatchDtos.*;
import EloRatingSystem.Dtos.PlayerDtos.PlayerRequestDto;
import EloRatingSystem.Dtos.PlayerDtos.PlayerResponseDto;
import EloRatingSystem.Models.Match;
import EloRatingSystem.Models.Player;
import EloRatingSystem.Models.SoloMatch;
import EloRatingSystem.Reporitories.MatchRepository;
import EloRatingSystem.Reporitories.MonthlyRatingRepository;
import EloRatingSystem.Reporitories.SoloMatchRepository;
import EloRatingSystem.Services.MatchService;
import EloRatingSystem.Services.PlayerService;
import EloRatingSystem.Services.SoloMatchService;
import EloRatingSystem.Services.TeamService;
import com.opencsv.bean.CsvToBeanBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.io.InputStreamReader;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/csv")
@PreAuthorize("hasRole('admin')")
public class CsvController {

    @Autowired
    MatchService matchService;
    @Autowired
    SoloMatchService soloMatchService;
    @Autowired
    PlayerService playerService;
    @Autowired
    TeamService teamService;

    @Autowired
    MatchRepository matchRepository;
    @Autowired
    SoloMatchRepository soloMatchRepository;
    @Autowired
    MonthlyRatingRepository monthlyRatingRepository;

    @PostMapping("/upload")
    public String uploadCsv(@RequestParam("file") MultipartFile file) {
        try {
            InputStreamReader reader = new InputStreamReader(file.getInputStream());
            // Read CSV file line by line
            List<csvMatchDto> csvMatches = new CsvToBeanBuilder<csvMatchDto>(reader)
                    .withType(csvMatchDto.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build()
                    .parse();

            csvMatches = csvMatches.stream()
                    .sorted(Comparator.comparingInt(csvMatchDto::getMatchId))
                    .collect(Collectors.toList());

            for (csvMatchDto csvMatchDto : csvMatches) {
                Mono<PlayerResponseDto> r_d = playerService.checkIfPlayerExists(csvMatchDto.getRedDefender()) ?
                        playerService.getByNameTag(csvMatchDto.getRedDefender()) :
                        playerService.newPlayer(new PlayerRequestDto(csvMatchDto.getRedDefender()));

                Mono<PlayerResponseDto> r_a = playerService.checkIfPlayerExists(csvMatchDto.getRedAttacker()) ?
                        playerService.getByNameTag(csvMatchDto.getRedAttacker()) :
                        playerService.newPlayer(new PlayerRequestDto(csvMatchDto.getRedAttacker()));

                Mono<PlayerResponseDto> b_d = playerService.checkIfPlayerExists(csvMatchDto.getBlueDefender()) ?
                        playerService.getByNameTag(csvMatchDto.getBlueDefender()) :
                        playerService.newPlayer(new PlayerRequestDto(csvMatchDto.getBlueDefender()));

                Mono<PlayerResponseDto> b_a = playerService.checkIfPlayerExists(csvMatchDto.getBlueAttacker()) ?
                        playerService.getByNameTag(csvMatchDto.getBlueAttacker()) :
                        playerService.newPlayer(new PlayerRequestDto(csvMatchDto.getBlueAttacker()));


                Mono<TeamResponseDto> teamRed = Mono.zip(r_d, r_a)
                        .flatMap(tuple -> {
                            PlayerResponseDto redDefender = tuple.getT1();
                            PlayerResponseDto redAttacker = tuple.getT2();

                            // Creating a team with all player IDs
                            TeamRequestDto teamRequest = new TeamRequestDto(
                                    redAttacker.getId(),
                                    redDefender.getId()
                            );
                            return teamService.newTeam(teamRequest);
                        });

                Mono<TeamResponseDto> teamBlue = Mono.zip(b_d, b_a)
                        .flatMap(tuple -> {
                            PlayerResponseDto blueDefender = tuple.getT1();
                            PlayerResponseDto blueAttacker = tuple.getT2();

                            // Creating a team with all player IDs
                            TeamRequestDto teamRequest = new TeamRequestDto(
                                    blueAttacker.getId(),
                                    blueDefender.getId()
                            );

                            return teamService.newTeam(teamRequest);
                        });

                Mono<Match2v2ResponseDto> Match = Mono.zip(r_d, r_a, b_d, b_a)
                        .flatMap(tuple -> {
                            PlayerResponseDto redAtk = tuple.getT1();
                            PlayerResponseDto redDef = tuple.getT2();
                            PlayerResponseDto blueAtk = tuple.getT3();
                            PlayerResponseDto blueDef = tuple.getT4();
                            return matchService.newMatch(new MatchRequestDto(redAtk.getId(), redDef.getId(), blueAtk.getId(), blueDef.getId(),
                                    csvMatchDto.getRedScore(), csvMatchDto.getBlueScore()));
                        });

                Match.flatMap(matchResponseDto -> {
                    Match match = matchRepository.findById(matchResponseDto.getId()).orElseThrow();
                    match.setDate(csvMatchDto.getDate());
                    matchRepository.save(match);
                    return null;
                }).subscribe();

            }


            return "CSV uploaded successfully!";
        } catch (Exception e) {
            return "Error processing CSV: " + e.getMessage();
        }
    }

    @PostMapping("/upload-json")
    public String uploadJson(@RequestBody List<Match> matches) {
        try {
            for (Match jsonMatchDto : matches) {
                Player redDefender = jsonMatchDto.getRedTeam().getDefender();
                Player redAttacker = jsonMatchDto.getRedTeam().getAttacker();
                Player blueDefender = jsonMatchDto.getBlueTeam().getDefender();
                Player blueAttacker = jsonMatchDto.getBlueTeam().getAttacker();

                Mono<PlayerResponseDto> r_d = playerService.checkIfPlayerExists(redDefender.getNameTag()) ?
                        playerService.getByNameTag(redDefender.getNameTag()) :
                        playerService.newPlayer(new PlayerRequestDto(redDefender.getNameTag()));

                Mono<PlayerResponseDto> r_a = playerService.checkIfPlayerExists(redAttacker.getNameTag()) ?
                        playerService.getByNameTag(redAttacker.getNameTag()) :
                        playerService.newPlayer(new PlayerRequestDto(redAttacker.getNameTag()));

                Mono<PlayerResponseDto> b_d = playerService.checkIfPlayerExists(blueDefender.getNameTag()) ?
                        playerService.getByNameTag(blueDefender.getNameTag()) :
                        playerService.newPlayer(new PlayerRequestDto(blueDefender.getNameTag()));

                Mono<PlayerResponseDto> b_a = playerService.checkIfPlayerExists(blueAttacker.getNameTag()) ?
                        playerService.getByNameTag(blueAttacker.getNameTag()) :
                        playerService.newPlayer(new PlayerRequestDto(blueAttacker.getNameTag()));

                Mono<Match2v2ResponseDto> matchMono = Mono.zip(r_d, r_a, b_d, b_a)
                        .flatMap(tuple -> {
                            PlayerResponseDto redAtk = tuple.getT1();
                            PlayerResponseDto redDef = tuple.getT2();
                            PlayerResponseDto blueAtk = tuple.getT3();
                            PlayerResponseDto blueDef = tuple.getT4();
                            return matchService.newMatch(new MatchRequestDto(redAtk.getId(), redDef.getId(), blueAtk.getId(), blueDef.getId(),
                                    jsonMatchDto.getRedTeamScore(), jsonMatchDto.getBlueTeamScore()));
                        });

                matchMono.flatMap(matchResponseDto -> {
                    Match match = matchRepository.findById(matchResponseDto.getId()).orElseThrow();
                    match.setDate(jsonMatchDto.getDate());
                    matchRepository.save(match);
                    return Mono.empty();
                }).subscribe();


            }

            return "JSON uploaded successfully!";
        } catch (Exception e) {
            return "Error processing JSON: " + e.getMessage();
        }
    }

    @PostMapping("/solo/upload-json")
    public String soloUploadJson(@RequestBody List<SoloMatchResponseDto> matches) {
        try {
            for (SoloMatchResponseDto jsonMatchDto : matches) {
                String red = jsonMatchDto.getRedPlayer();
                String blue = jsonMatchDto.getBluePlayer();

                Mono<PlayerResponseDto> r_d = playerService.checkIfPlayerExists(red) ?
                        playerService.getByNameTag(red) :
                        playerService.newPlayer(new PlayerRequestDto(red));

                Mono<PlayerResponseDto> r_a = playerService.checkIfPlayerExists(blue) ?
                        playerService.getByNameTag(blue) :
                        playerService.newPlayer(new PlayerRequestDto(blue));

                Mono<SoloMatchResponseDto> matchMono = Mono.zip(r_d, r_a)
                        .flatMap(tuple -> {
                            PlayerResponseDto r = tuple.getT1();
                            PlayerResponseDto b = tuple.getT2();
                            return soloMatchService.newSoloMatch(new SoloMatchRequestDto(r.getId(), b.getId(),
                                    jsonMatchDto.getRedScore(), jsonMatchDto.getBlueScore()));
                        });

                matchMono.flatMap(matchResponseDto -> {
                    SoloMatch match = soloMatchRepository.findById(matchResponseDto.getId()).orElseThrow();
                    match.setDate(jsonMatchDto.getDate());
                    soloMatchRepository.save(match);
                    return Mono.empty();
                }).subscribe();

            }

            return "JSON uploaded successfully!";
        } catch (Exception e) {
            return "Error processing JSON: " + e.getMessage();
        }
    }
}
