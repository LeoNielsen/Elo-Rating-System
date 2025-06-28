package EloRatingSystem.Controllers;

import EloRatingSystem.Dtos.*;
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

                Mono<Match2v2ResponseDto> Match = Mono.zip(teamRed, teamBlue)
                        .flatMap(tuple -> {
                            TeamResponseDto redTeam = tuple.getT1();
                            TeamResponseDto blueTeam = tuple.getT2();
                            return matchService.newMatch(new MatchRequestDto(redTeam.getId(), blueTeam.getId(), csvMatchDto.getRedScore(), csvMatchDto.getBlueScore()));
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

                Mono<TeamResponseDto> teamRed = Mono.zip(r_d, r_a)
                        .flatMap(tuple -> {
                            PlayerResponseDto rd = tuple.getT1();
                            PlayerResponseDto ra = tuple.getT2();
                            return teamService.newTeam(new TeamRequestDto(ra.getId(), rd.getId()));
                        });

                Mono<TeamResponseDto> teamBlue = Mono.zip(b_d, b_a)
                        .flatMap(tuple -> {
                            PlayerResponseDto bd = tuple.getT1();
                            PlayerResponseDto ba = tuple.getT2();
                            return teamService.newTeam(new TeamRequestDto(ba.getId(), bd.getId()));
                        });

                Mono<Match2v2ResponseDto> matchMono = Mono.zip(teamRed, teamBlue)
                        .flatMap(tuple -> {
                            TeamResponseDto red = tuple.getT1();
                            TeamResponseDto blue = tuple.getT2();
                            return matchService.newMatch(new MatchRequestDto(red.getId(), blue.getId(),
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
                PlayerResponseDto red = jsonMatchDto.getRedPlayer();
                PlayerResponseDto blue = jsonMatchDto.getBluePlayer();

                Mono<PlayerResponseDto> r_d = playerService.checkIfPlayerExists(red.getNameTag()) ?
                        playerService.getByNameTag(red.getNameTag()) :
                        playerService.newPlayer(new PlayerRequestDto(red.getNameTag()));

                Mono<PlayerResponseDto> r_a = playerService.checkIfPlayerExists(blue.getNameTag()) ?
                        playerService.getByNameTag(blue.getNameTag()) :
                        playerService.newPlayer(new PlayerRequestDto(blue.getNameTag()));

                Mono<SoloMatchResponseDto> matchMono = Mono.zip(r_d, r_a)
                        .flatMap(tuple -> {
                            PlayerResponseDto r = tuple.getT1();
                            PlayerResponseDto b = tuple.getT2();
                            return soloMatchService.newSoloMatch(new SoloMatchRequestDto(r.getId(), b.getId(),
                                    jsonMatchDto.getRedTeamScore(), jsonMatchDto.getBlueTeamScore()));
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
