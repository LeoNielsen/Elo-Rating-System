package EloRatingSystem.Modules.Monthly.Controllers;

import EloRatingSystem.Modules.Monthly.Dtos.MonthlyWinnerDto;
import EloRatingSystem.Modules.Monthly.Services.MonthlyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("monthly")
public class MonthlyController {

    @Autowired
    MonthlyService monthlyService;
    @GetMapping("/winner/last")
    public Mono<List<MonthlyWinnerDto>> getLastMonthWinner() {
        return monthlyService.getLastMonthWinner();
    }

    @GetMapping("/winner/all")
    public Mono<List<MonthlyWinnerDto>> getAllMonthWinners() {
        return monthlyService.getAllMonthWinners();
    }

    @PreAuthorize("hasRole('admin')")
    @GetMapping("/winner")
    public void makeMonthlyWinner() {
        monthlyService.setMonthlyWinner();
    }
}
