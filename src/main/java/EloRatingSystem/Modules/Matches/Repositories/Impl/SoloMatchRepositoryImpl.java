package EloRatingSystem.Modules.Matches.Repositories.Impl;

import EloRatingSystem.Modules.Matches.Models.SoloMatch;
import EloRatingSystem.Modules.Matches.Repositories.Adapter.MatchRepositoryAdapter;
import EloRatingSystem.Modules.Matches.Repositories.repo.SoloMatchRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

public class SoloMatchRepositoryImpl implements MatchRepositoryAdapter<SoloMatch> {

    @Autowired
    SoloMatchRepository soloMatchRepository;
    @Override
    public List<SoloMatch> getAllMatches() {
        return soloMatchRepository.findAll();
    }

    @Override
    public Optional<SoloMatch> getMatchById(Long id) {
        return soloMatchRepository.findById(id);
    }

    @Override
    public List<SoloMatch> getRecentMatches() {
        return soloMatchRepository.findTop100ByOrderByIdDesc();
    }

    @Override
    public SoloMatch save(SoloMatch match) {
        return soloMatchRepository.save(match);
    }
}
