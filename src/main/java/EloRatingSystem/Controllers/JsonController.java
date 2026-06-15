package EloRatingSystem.Controllers;

import EloRatingSystem.Modules.Matches.Dtos.TeamMatchResponseDto;
import EloRatingSystem.Modules.Matches.Dtos.TeamMatchRequestDto;
import EloRatingSystem.Modules.Matches.Dtos.SoloMatchRequestDto;
import EloRatingSystem.Modules.Matches.Dtos.SoloMatchResponseDto;
import EloRatingSystem.Modules.Matches.Models.TeamMatch;
import EloRatingSystem.Modules.Matches.Models.SoloMatch;
import EloRatingSystem.Modules.Matches.Repositories.repo.TeamMatchRepository;
import EloRatingSystem.Modules.Matches.Repositories.repo.SoloMatchRepository;
import EloRatingSystem.Modules.Matches.Services.MatchService;
import EloRatingSystem.Modules.Matches.Services.SoloMatchService;
import EloRatingSystem.Modules.player.Dtos.PlayerRequestDto;
import EloRatingSystem.Modules.player.Dtos.PlayerResponseDto;
import EloRatingSystem.Modules.player.Services.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/Json")
@PreAuthorize("hasRole('admin')")
public class JsonController {

    @Autowired
    MatchService matchService;
    @Autowired
    SoloMatchService soloMatchService;
    @Autowired
    PlayerService playerService;

    @Autowired
    TeamMatchRepository matchRepository;
    @Autowired
    SoloMatchRepository soloMatchRepository;

    @PostMapping("/upload")
    public Mono<String> uploadJson(@RequestBody List<TeamMatchResponseDto> matches) {

        return Flux.fromIterable(matches)
                .concatMap(jsonMatchDto -> {

                    PlayerRequestDto redDefender = new PlayerRequestDto(jsonMatchDto.getRedDef());
                    PlayerRequestDto redAttacker = new PlayerRequestDto(jsonMatchDto.getRedAtk());
                    PlayerRequestDto blueDefender = new PlayerRequestDto(jsonMatchDto.getBlueDef());
                    PlayerRequestDto blueAttacker = new PlayerRequestDto(jsonMatchDto.getBlueAtk());

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

                    return Mono.zip(r_d, r_a, b_d, b_a)
                            .flatMap(tuple -> {
                                TeamMatchRequestDto req = new TeamMatchRequestDto(
                                        tuple.getT2().getId(),
                                        tuple.getT1().getId(),
                                        tuple.getT3().getId(),
                                        tuple.getT4().getId(),
                                        jsonMatchDto.getRedScore(),
                                        jsonMatchDto.getBlueScore()
                                );
                                return matchService.newMatch(req);
                            })
                            .flatMap(matchResponse -> {
                                TeamMatch match = matchRepository.findById(matchResponse.getId()).orElseThrow();
                                match.setDate(jsonMatchDto.getDate());
                                return Mono.just(matchRepository.save(match));
                            });
                })
                .then(Mono.just("JSON uploaded successfully!"));
    }

    @PostMapping("/solo/upload")
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
