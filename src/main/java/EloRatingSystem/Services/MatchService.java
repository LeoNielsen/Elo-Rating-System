package EloRatingSystem.Services;

import EloRatingSystem.Models.Match;
import EloRatingSystem.Reporitories.MatchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class MatchService {

    @Autowired
    MatchRepository matchRepository;

    public Mono<Match> newMatch(Match match) {
       return Mono.just(matchRepository.save(match));
    }
}

