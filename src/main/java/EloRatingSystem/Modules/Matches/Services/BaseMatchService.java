package EloRatingSystem.Modules.Matches.Services;

import EloRatingSystem.Modules.Matches.Models.BaseMatch;
import EloRatingSystem.Modules.Matches.Repositories.Adapter.MatchRepositoryAdapter;

import java.util.List;
import java.util.Optional;

public abstract class BaseMatchService<T extends BaseMatch> {

    protected abstract MatchRepositoryAdapter<T> getRepository();

    public List<T> getAllMatches() {
        return getRepository().getAllMatches();
    }

    public Optional<T> getMatchById(Long Id) {
        return getRepository().getMatchById(Id);
    }

    public List<T> getRecentMatches(){
        return getRepository().getRecentMatches();
    }

}

