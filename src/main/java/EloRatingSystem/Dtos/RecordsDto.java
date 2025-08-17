package EloRatingSystem.Dtos;

import lombok.Data;

@Data
public class RecordsDto {
    private RecordDto highestRating2v2;
    private RecordDto lowestRating2v2;
    private RecordDto highestRating1v1;
    private RecordDto lowestRating1v1;
    private RecordDto mostGames2v2;
    private RecordDto mostGames1v1;
    private RecordDto mostWins2v2;
    private RecordDto mostAttackerWins;
    private RecordDto mostDefenderWins;
    private RecordDto mostWins1v1;
    private RecordDto mostLost2v2;
    private RecordDto mostAttackerLost;
    private RecordDto mostDefenderLost;
    private RecordDto mostLost1v1;
    private RecordDto mostGoals2v2;
    private RecordDto mostGoals1v1;
    private RecordDto longestWinStreak2v2;
    private RecordDto longestWinStreak1v1;
    private RecordDto highestDailyEloChange2v2;
    private RecordDto lowestDailyEloChange2v2;
    private RecordDto highestDailyEloChange1v1;
    private RecordDto lowestDailyEloChange1v1;
}
