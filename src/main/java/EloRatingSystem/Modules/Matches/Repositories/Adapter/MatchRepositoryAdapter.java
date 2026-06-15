package EloRatingSystem.Modules.Matches.Repositories.Adapter;

import java.util.List;
import java.util.Optional;

public interface MatchRepositoryAdapter<T> {

    List<T> getAllMatches();

    Optional<T> getMatchById(Long id);

    List<T> getRecentMatches();

    T save(T match);
}
