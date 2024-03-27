package EloRatingSystem.Services;

import EloRatingSystem.Dtos.MatchRequestDto;
import EloRatingSystem.Dtos.MatchResponseDto;
import EloRatingSystem.Exception.ApiException;
import EloRatingSystem.Models.Match;
import EloRatingSystem.Models.Team;
import EloRatingSystem.Reporitories.MatchRepository;
import EloRatingSystem.Reporitories.TeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class MatchService {

    @Autowired
    MatchRepository matchRepository;
    @Autowired
    TeamRepository teamRepository;
    @Autowired
    RatingService ratingService;

    public Mono<List<MatchResponseDto>> getAllMatches() {
        List<Match> matches = matchRepository.findAll();
        List<MatchResponseDto> matchResponseDtoList = new ArrayList<>();
        for (Match match : matches) {
            matchResponseDtoList.add(new MatchResponseDto(match));
        }

        return Mono.just(matchResponseDtoList);
    }

    public Mono<MatchResponseDto> newMatch(MatchRequestDto requestDto) {
        Optional<Team> redTeamOptional = teamRepository.findById(requestDto.getRedTeamId());
        Optional<Team> blueTeamOptional = teamRepository.findById(requestDto.getBlueTeamId());

        if (blueTeamOptional.isPresent() && redTeamOptional.isPresent()) {
            Team redTeam = redTeamOptional.get();
            Team blueTeam = blueTeamOptional.get();

            Match match = matchRepository.save(new Match(redTeam, blueTeam,
                    requestDto.getRedTeamScore(), requestDto.getBlueTeamScore()));

            match = ratingService.newRating(match);

            match = matchRepository.save(match);

            return Mono.just(new MatchResponseDto(match));
        }

        return Mono.error(new ApiException(
                String.format("Either team %s or %s does not exits", requestDto.getRedTeamId(), requestDto.getBlueTeamId())
                , HttpStatus.BAD_REQUEST));
    }

    public Mono<MatchResponseDto> getMatchById(Long id) {
        Optional<Match> match = matchRepository.findById(id);
        return match.map(value -> Mono.just(new MatchResponseDto(value)))
                .orElseGet(() -> Mono.error(new ApiException(String.format("match %s doesn't exist", id), HttpStatus.BAD_REQUEST)));
    }
}
