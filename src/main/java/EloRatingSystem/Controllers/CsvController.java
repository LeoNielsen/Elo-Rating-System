package EloRatingSystem.Controllers;

import EloRatingSystem.Dtos.*;
import EloRatingSystem.Models.Match;
import EloRatingSystem.Reporitories.MatchRepository;
import EloRatingSystem.Services.MatchService;
import EloRatingSystem.Services.PlayerService;
import EloRatingSystem.Services.TeamService;
import com.opencsv.bean.CsvToBeanBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.io.InputStreamReader;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/csv")
public class CsvController {

    @Autowired
    MatchService matchService;
    @Autowired
    PlayerService playerService;
    @Autowired
    TeamService teamService;

    @Autowired
    MatchRepository matchRepository;

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

                Mono<MatchResponseDto> Match = Mono.zip(teamRed, teamBlue)
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
}
