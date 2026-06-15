package EloRatingSystem.Modules.Matches.Repositories.Impl;

import EloRatingSystem.Modules.Matches.Models.TeamMatch;
import EloRatingSystem.Modules.Matches.Repositories.Adapter.MatchRepositoryAdapter;
import EloRatingSystem.Modules.Matches.Repositories.repo.TeamMatchRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

public class TeamMatchRepositoryImpl implements MatchRepositoryAdapter<TeamMatch> {

    @Autowired
    TeamMatchRepository teamMatchRepository;

    @Override
    public List<TeamMatch> getAllMatches() {
        return teamMatchRepository.findAll();
    }

    @Override
    public Optional<TeamMatch> getMatchById(Long id) {
        return teamMatchRepository.findById(id);
    }

    @Override
    public List<TeamMatch> getRecentMatches() {
        return teamMatchRepository.findTop100ByOrderByIdDesc();
    }

    @Override
    public TeamMatch save(TeamMatch match) {
        return teamMatchRepository.save(match);
    }
}
