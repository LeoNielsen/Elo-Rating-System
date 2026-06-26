package EloRatingSystem.Modules.Matches.Services;

import EloRatingSystem.Modules.Matches.Models.Match;
import EloRatingSystem.Modules.Matches.Models.SoloMatch;
import EloRatingSystem.Modules.Matches.Repositories.MatchRepository;
import EloRatingSystem.Modules.Matches.Repositories.SoloMatchRepository;
import EloRatingSystem.Modules.Matches.Utils.MatchUtils;
import EloRatingSystem.Modules.Rating.Services.MonthlyRatingService;
import EloRatingSystem.Modules.Rating.Services.RatingService;
import EloRatingSystem.Modules.Rating.Services.SoloRatingService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class MatchDeleteService {

    @Autowired
    MatchRepository matchRepository;
    @Autowired
    RatingService ratingService;
    @Autowired
    SoloRatingService soloRatingService;
    @Autowired
    MonthlyRatingService monthlyRatingService;
    @Autowired
    SoloMatchRepository soloMatchRepository;
    @Autowired
    MatchUtils matchUtils;

    @Transactional
    private void deleteMatch(Match match) {
        matchUtils.removeMatchStats(match);
        matchRepository.deleteById(match.getId());
    }

    @Transactional
    public void deleteMatch(SoloMatch match) {
        matchUtils.removeMatchStats(match);
        soloMatchRepository.deleteById(match.getId());
    }

    @Transactional
    public void deleteMatchById(long id) {

        List<Match> matches = matchRepository.findAllByIdGreaterThanEqual(id);

        if (matches.isEmpty()) {
            return;
        }

        matches.sort(Comparator.comparingLong(Match::getId));

        for (int i = matches.size() - 1; i >= 0; i--) {
            matchUtils.removeMatchStats(matches.get(i));
        }

        matchRepository.deleteById(matches.get(0).getId());
        matches.remove(0);

        for (Match m : matches) {
            Match match = matchRepository.save(m);
            ratingService.newRating(match);
            monthlyRatingService.newRating(match);
        }
    }

    @Transactional
    public void deleteSoloMatchById(long id) {

        List<SoloMatch> matches = soloMatchRepository.findAllByIdGreaterThanEqual(id);

        if (matches.isEmpty()) {
            return;
        }

        matches.sort(Comparator.comparingLong(SoloMatch::getId));

        for (int i = matches.size() - 1; i >= 0; i--) {
            matchUtils.removeMatchStats(matches.get(i));
        }

        soloMatchRepository.deleteById(matches.get(0).getId());
        matches.remove(0);

        for (SoloMatch m : matches) {
            SoloMatch match = soloMatchRepository.save(m);
            soloRatingService.newSoloRating(match);
        }
    }
}
