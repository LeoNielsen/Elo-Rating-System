package EloRatingSystem.Controllers;

import EloRatingSystem.Dtos.MatchDtos.Match2v2ResponseDto;
import EloRatingSystem.Dtos.MatchDtos.MatchRequestDto;
import EloRatingSystem.Dtos.MatchDtos.SoloMatchRequestDto;
import EloRatingSystem.Dtos.MatchDtos.SoloMatchResponseDto;
import EloRatingSystem.Dtos.PlayerDtos.PlayerRequestDto;
import EloRatingSystem.Dtos.PlayerDtos.PlayerResponseDto;
import EloRatingSystem.Models.Match;
import EloRatingSystem.Models.SoloMatch;
import EloRatingSystem.Reporitories.MatchRepository;
import EloRatingSystem.Reporitories.SoloMatchRepository;
import EloRatingSystem.Services.MatchService;
import EloRatingSystem.Services.PlayerService;
import EloRatingSystem.Services.SoloMatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
    MatchRepository matchRepository;
    @Autowired
    SoloMatchRepository soloMatchRepository;

    @PostMapping("/upload")
    public String uploadJson(@RequestBody List<Match2v2ResponseDto> matches) {
        try {
            for (Match2v2ResponseDto jsonMatchDto : matches) {
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

                Mono<Match2v2ResponseDto> matchMono = Mono.zip(r_d, r_a, b_d, b_a)
                        .flatMap(tuple -> {
                            PlayerResponseDto redAtk = tuple.getT1();
                            PlayerResponseDto redDef = tuple.getT2();
                            PlayerResponseDto blueAtk = tuple.getT3();
                            PlayerResponseDto blueDef = tuple.getT4();
                            return matchService.newMatch(new MatchRequestDto(redAtk.getId(), redDef.getId(), blueAtk.getId(), blueDef.getId(),
                                    jsonMatchDto.getRedScore(), jsonMatchDto.getBlueScore()));
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
