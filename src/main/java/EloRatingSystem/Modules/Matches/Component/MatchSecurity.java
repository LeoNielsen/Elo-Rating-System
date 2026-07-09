package EloRatingSystem.Modules.Matches.Component;

import EloRatingSystem.Modules.Matches.Repositories.MatchRepository;
import EloRatingSystem.Modules.Matches.Repositories.SoloMatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component("matchSecurity")
@RequiredArgsConstructor
public class MatchSecurity {

    @Autowired
    private final MatchRepository matchRepository;
    @Autowired
    private final SoloMatchRepository soloMatchRepository;

    public boolean isMatchOwner(Long matchId, Authentication authentication) {
        var match = matchRepository.findById(matchId).orElse(null);
        if (match == null) return false;

        if (!match.getDate().toLocalDate().equals(LocalDate.now())) {
            return false;
        }

        var jwt = ((JwtAuthenticationToken) authentication).getToken();
        String username = jwt.getClaim("preferred_username");

        return match.getCreatedBy().equals(username);
    }

    public boolean isSoloMatchOwner(Long matchId, Authentication authentication) {
        var match = soloMatchRepository.findById(matchId).orElse(null);
        if (match == null) return false;

        if (!match.getDate().toLocalDate().equals(LocalDate.now())) {
            return false;
        }

        var jwt = ((JwtAuthenticationToken) authentication).getToken();
        String username = jwt.getClaim("preferred_username");

        return match.getCreatedBy().equals(username);
    }



}
