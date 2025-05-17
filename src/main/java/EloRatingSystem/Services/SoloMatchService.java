package EloRatingSystem.Services;

import EloRatingSystem.Dtos.SoloMatchRequestDto;
import EloRatingSystem.Dtos.SoloMatchResponseDto;
import EloRatingSystem.Exception.ApiException;
import EloRatingSystem.Models.Player;
import EloRatingSystem.Models.SoloMatch;
import EloRatingSystem.Reporitories.PlayerRepository;
import EloRatingSystem.Reporitories.SoloMatchRepository;
import EloRatingSystem.Services.RatingServices.SoloRatingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class SoloMatchService {
    @Autowired
    SoloMatchRepository soloMatchRepository;
    @Autowired
    PlayerRepository playerRepository;
    @Autowired
    SoloRatingService soloRatingService;
    @Autowired
    RegenerateService regenerateService;

    public Mono<List<SoloMatchResponseDto>> getRecentMatches() {
        List<SoloMatch> matches = soloMatchRepository.findTop100ByOrderByIdDesc();
        List<SoloMatchResponseDto> matchResponseDtoList = new ArrayList<>();
        for (SoloMatch match : matches) {
            matchResponseDtoList.add(new SoloMatchResponseDto(match));
        }

        return Mono.just(matchResponseDtoList);
    }

    public Mono<SoloMatchResponseDto> getSoloMatchById(Long id) {
        Optional<SoloMatch> match = soloMatchRepository.findById(id);
        return match.map(value -> Mono.just(new SoloMatchResponseDto(value)))
                .orElseGet(() -> Mono.error(new ApiException(String.format("match %s doesn't exist", id), HttpStatus.BAD_REQUEST)));

    }

    public Mono<SoloMatchResponseDto> newSoloMatch(SoloMatchRequestDto requestDto) {
        Optional<Player> redPlayerOptional = playerRepository.findById(requestDto.getRedPlayerId());
        Optional<Player> bluePlayerOptional = playerRepository.findById(requestDto.getBluePlayerId());

        if (bluePlayerOptional.isPresent() && redPlayerOptional.isPresent()) {
            Player redPlayer = redPlayerOptional.get();
            Player bluePlayer = bluePlayerOptional.get();

            SoloMatch match = soloMatchRepository.save(new SoloMatch(new Date(System.currentTimeMillis()), redPlayer, bluePlayer,
                    requestDto.getRedScore(), requestDto.getBlueScore()));

            match = soloRatingService.newSoloRating(match);

            match = soloMatchRepository.save(match);

            return Mono.just(new SoloMatchResponseDto(match));
        }

        return Mono.error(new ApiException(
                String.format("Either player %s or %s does not exits", requestDto.getRedPlayerId(), requestDto.getBluePlayerId())
                , HttpStatus.BAD_REQUEST));
    }

    public void deleteLatestSoloMatch() {
        SoloMatch match = soloMatchRepository.findTop1ByOrderByIdDesc().orElseThrow();
        soloRatingService.deleteRatingsBySoloMatch(match.getId());

        Player redPlayer = match.getRedPlayer();
        Player bluePlayer = match.getBluePlayer();

        soloMatchRepository.deleteById(match.getId());

        regenerateService.regenerateSoloPlayerStatistics(redPlayer);
        regenerateService.regenerateSoloPlayerStatistics(bluePlayer);
    }
}
