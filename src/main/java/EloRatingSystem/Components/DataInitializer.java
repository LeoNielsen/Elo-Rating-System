package EloRatingSystem.Components;

import EloRatingSystem.Models.Achievement.Achievement;
import EloRatingSystem.Models.Achievement.AchievementMetric;
import EloRatingSystem.Models.Achievement.AchievementType;
import EloRatingSystem.Models.Achievement.GameType;
import EloRatingSystem.Reporitories.Achievements.AchievementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    AchievementRepository achievementRepository;

    @Override
    public void run(String... args) {
        addIfMissing("rating_1300", "The Climb", "Reach 1300 rating", GameType.TEAMS, AchievementType.RATING_REACHED, AchievementMetric.RATING, 1300);
        addIfMissing("rating_1350", "Steps to Glory", "Reach 1350 rating", GameType.TEAMS, AchievementType.RATING_REACHED, AchievementMetric.RATING, 1350);
        addIfMissing("rating_1400", "Rating Rush", "Reach 1400 rating", GameType.TEAMS, AchievementType.RATING_REACHED, AchievementMetric.RATING, 1400);
        addIfMissing("rating_1450", "Rating Raider", "Reach 1450 rating", GameType.TEAMS, AchievementType.RATING_REACHED, AchievementMetric.RATING, 1450);
        addIfMissing("rating_1500", "Peak Performer", "Reach 1500 rating", GameType.TEAMS, AchievementType.RATING_REACHED, AchievementMetric.RATING, 1500);

        addIfMissing("rating_1300_solo", "Path of One", "Reach 1300 rating in solo", GameType.SOLO, AchievementType.RATING_REACHED_SOLO, AchievementMetric.RATING, 1300);
        addIfMissing("rating_1350_solo", "Climber", "Reach 1350 rating in solo", GameType.SOLO, AchievementType.RATING_REACHED_SOLO, AchievementMetric.RATING, 1350);
        addIfMissing("rating_1400_solo", "Solo Rush", "Reach 1400 rating in solo", GameType.SOLO, AchievementType.RATING_REACHED_SOLO, AchievementMetric.RATING, 1400);
        addIfMissing("rating_1450_solo", "Lone Raider", "Reach 1450 rating in solo", GameType.SOLO, AchievementType.RATING_REACHED_SOLO, AchievementMetric.RATING, 1450);
        addIfMissing("rating_1500_solo", "Solo Performer", "Reach 1500 rating in solo", GameType.SOLO, AchievementType.RATING_REACHED_SOLO, AchievementMetric.RATING, 1500);

        addIfMissing("wins_50", "First Taste of Victory", "Reach 50 wins", GameType.TEAMS, AchievementType.TOTAL_WINS, AchievementMetric.WINS, 50);
        addIfMissing("wins_100", "Winning Ways", "Reach 100 wins", GameType.TEAMS, AchievementType.TOTAL_WINS, AchievementMetric.WINS, 100);
        addIfMissing("wins_150", "Relentless", "Reach 150 wins", GameType.TEAMS, AchievementType.TOTAL_WINS, AchievementMetric.WINS, 150);
        addIfMissing("wins_200", "Victory March", "Reach 200 wins", GameType.TEAMS, AchievementType.TOTAL_WINS, AchievementMetric.WINS, 200);
        addIfMissing("wins_250", "Victory Machine", "Reach 250 wins", GameType.TEAMS, AchievementType.TOTAL_WINS, AchievementMetric.WINS, 250);

        addIfMissing("wins_50_solo", "Solo Starter", "Reach 50 wins in solo", GameType.SOLO, AchievementType.TOTAL_WINS_SOLO, AchievementMetric.WINS, 50);
        addIfMissing("wins_100_solo", "The Solo Standard", "Reach 100 wins in solo", GameType.SOLO, AchievementType.TOTAL_WINS_SOLO, AchievementMetric.WINS, 100);
        addIfMissing("wins_150_solo", "Unstoppable Solo", "Reach 150 wins in solo", GameType.SOLO, AchievementType.TOTAL_WINS_SOLO, AchievementMetric.WINS, 150);
        addIfMissing("wins_200_solo", "One-Man Army", "Reach 200 wins in solo", GameType.SOLO, AchievementType.TOTAL_WINS_SOLO, AchievementMetric.WINS, 200);
        addIfMissing("wins_250_solo", "Solo Supreme", "Reach 250 wins in solo", GameType.SOLO, AchievementType.TOTAL_WINS_SOLO, AchievementMetric.WINS, 250);


        addIfMissing("win_streak_5", "Heating Up", "win 5 times in a row", GameType.TEAMS, AchievementType.WIN_STREAK, AchievementMetric.WIN_STREAK, 5);
        addIfMissing("win_streak_10", "Hot Streak", "win 10 times in a row", GameType.TEAMS, AchievementType.WIN_STREAK, AchievementMetric.WIN_STREAK, 10);
        addIfMissing("win_streak_15", "On Fire", "win 15 times in a row", GameType.TEAMS, AchievementType.WIN_STREAK, AchievementMetric.WIN_STREAK, 15);
        addIfMissing("win_streak_20", "Burning Bright", "win 20 times in a row", GameType.TEAMS, AchievementType.WIN_STREAK, AchievementMetric.WIN_STREAK, 20);
        addIfMissing("win_streak_25", "The Inferno", "win 25 times in a row", GameType.TEAMS, AchievementType.WIN_STREAK, AchievementMetric.WIN_STREAK, 25);

        addIfMissing("win_streak_5_solo", "Getting Warm", "win 5 times in a row in solo", GameType.SOLO, AchievementType.WIN_STREAK_SOLO, AchievementMetric.WIN_STREAK, 5);
        addIfMissing("win_streak_10_solo", "Hot Solo", "win 10 times in a row in solo", GameType.SOLO, AchievementType.WIN_STREAK_SOLO, AchievementMetric.WIN_STREAK, 10);
        addIfMissing("win_streak_15_solo", "Solo Fire", "win 15 times in a row in solo", GameType.SOLO, AchievementType.WIN_STREAK_SOLO, AchievementMetric.WIN_STREAK, 15);
        addIfMissing("win_streak_20_solo", "Burning Solo", "win 20 times in a row in solo", GameType.SOLO, AchievementType.WIN_STREAK_SOLO, AchievementMetric.WIN_STREAK, 20);
        addIfMissing("win_streak_25_solo", "Lone Inferno", "win 25 times in a row in solo", GameType.SOLO, AchievementType.WIN_STREAK_SOLO, AchievementMetric.WIN_STREAK, 25);

        addIfMissing("win_10_zero_as_defender", "Absolute Defense", "win a game 10 - 0 as a defender", GameType.TEAMS, AchievementType.WIN_10_ZERO_AS_DEFENDER, AchievementMetric.WINS, 1);
        addIfMissing("win_10_zero_as_attacker", "Offensive Overdrive", "win a game 10 - 0 as an attacker", GameType.TEAMS, AchievementType.WIN_10_ZERO_AS_ATTACKER, AchievementMetric.WINS, 1);
        addIfMissing("win_10_zero_Solo", "Solo Slaughter", "win a game 10 - 0 in 1v1", GameType.SOLO, AchievementType.WIN_10_ZERO_SOLO, AchievementMetric.WINS, 1);

        addIfMissing("monthly_2v2_wins", "First Crown", "win monthly 2v2", GameType.MONTHLY, AchievementType.TOTAL_MONTHLY_2V2_WINS, AchievementMetric.WINS, 1);
        addIfMissing("monthly_2v2_wins_3", "Monthly Master", "win monthly 2v2 3 times", GameType.MONTHLY, AchievementType.TOTAL_MONTHLY_2V2_WINS, AchievementMetric.WINS, 3);
        addIfMissing("monthly_2v2_wins_6", "Seasonal Champion", "win monthly 2v2 6 times", GameType.MONTHLY, AchievementType.TOTAL_MONTHLY_2V2_WINS, AchievementMetric.WINS, 6);

        addIfMissing("win_agains_higher_rated_100", "Punching Up", "Win against a player rated 100 ELO higher than you", GameType.TEAMS, AchievementType.WIN_AGAINST_HIGHER_RATED, AchievementMetric.RATING, 100);
        addIfMissing("win_agains_higher_rated_200", "The Underdog", "Win against a player rated 200 ELO higher than you", GameType.TEAMS, AchievementType.WIN_AGAINST_HIGHER_RATED, AchievementMetric.RATING, 200);
        addIfMissing("win_agains_higher_rated_300", "Giant Slayer", "Win against a player rated 300 ELO higher than you", GameType.TEAMS, AchievementType.WIN_AGAINST_HIGHER_RATED, AchievementMetric.RATING, 300);
        addIfMissing("win_agains_higher_rated_400", "No Respect", "Win against a player rated 400 ELO higher than you", GameType.TEAMS, AchievementType.WIN_AGAINST_HIGHER_RATED, AchievementMetric.RATING, 400);
        addIfMissing("win_agains_higher_rated_500", "Defying the Odds", "Win against a player rated 500 ELO higher than you", GameType.TEAMS, AchievementType.WIN_AGAINST_HIGHER_RATED, AchievementMetric.RATING, 500);

        addIfMissing("win_agains_higher_rated_100_solo", "Swing First", "win against a player rated 100 ELO higher then you in solo", GameType.SOLO, AchievementType.WIN_AGAINST_HIGHER_RATED_SOLO, AchievementMetric.RATING, 100);
        addIfMissing("win_agains_higher_rated_200_solo", "Going Solo", "win against a player rated 200 ELO higher then you in solo", GameType.SOLO, AchievementType.WIN_AGAINST_HIGHER_RATED_SOLO, AchievementMetric.RATING, 200);
        addIfMissing("win_agains_higher_rated_300_solo", "Lone Slayer", "win against a player rated 300 ELO higher then you in solo", GameType.SOLO, AchievementType.WIN_AGAINST_HIGHER_RATED_SOLO, AchievementMetric.RATING, 300);
        addIfMissing("win_agains_higher_rated_400_solo", "All Eyes on Me", "win against a player rated 400 ELO higher then you in solo", GameType.SOLO, AchievementType.WIN_AGAINST_HIGHER_RATED_SOLO, AchievementMetric.RATING, 400);
        addIfMissing("win_agains_higher_rated_500_solo", "One Against the World", "win against a player rated 500 ELO higher then you in solo", GameType.SOLO, AchievementType.WIN_AGAINST_HIGHER_RATED_SOLO, AchievementMetric.RATING, 500);
    }

    private void addIfMissing(String code, String name, String desc, GameType gameType, AchievementType type, AchievementMetric metric, int amount) {
        if (achievementRepository.findByCode(code).isEmpty()) {
            achievementRepository.save(new Achievement(code, name, desc, gameType, type, metric, amount));
        }
    }
}
