package EloRatingSystem.Dtos;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvDate;
import lombok.Data;

import java.sql.Date;

@Data
public class csvMatchDto {

    @CsvBindByName(column = "MatchID")
    private int matchId;

    @CsvBindByName(column = "Date")
    @CsvDate("dd/MM/yyyy")
    private Date date;

    @CsvBindByName(column = "Red Defender")
    private String redDefender;

    @CsvBindByName(column = "Red Attacker")
    private String redAttacker;

    @CsvBindByName(column = "Blue Defender")
    private String blueDefender;

    @CsvBindByName(column = "Blue Attacker")
    private String blueAttacker;

    @CsvBindByName(column = "Red Score")
    private int redScore;

    @CsvBindByName(column = "Blue Score")
    private int blueScore;

}
