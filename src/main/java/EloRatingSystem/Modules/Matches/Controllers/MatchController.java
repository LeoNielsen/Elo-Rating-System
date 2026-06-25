package EloRatingSystem.Modules.Matches.Controllers;

import EloRatingSystem.Modules.Matches.Dtos.SoloMatchRequestDto;
import EloRatingSystem.Modules.Matches.Dtos.SoloMatchResponseDto;
import EloRatingSystem.Modules.Matches.Dtos.TeamMatchRequestDto;
import EloRatingSystem.Modules.Matches.Dtos.TeamMatchResponseDto;
import EloRatingSystem.Modules.Matches.Services.MatchDeleteService;
import EloRatingSystem.Modules.Matches.Services.MatchService;
import EloRatingSystem.Modules.Matches.Services.MatchUpdateService;
import EloRatingSystem.Modules.Matches.Services.SoloMatchService;
import EloRatingSystem.Modules.Stats.Dtos.MatchStatisticsDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("match")
@Slf4j
public class MatchController {

    @Autowired
    MatchService matchService;
    @Autowired
    MatchDeleteService matchDeleteService;
    @Autowired
    MatchUpdateService matchUpdateService;
    @Autowired
    SoloMatchService soloMatchService;

    @GetMapping("/{id}")
    public Mono<TeamMatchResponseDto> getMatchById(@PathVariable Long id) {
        return matchService.getMatchById(id);
    }

    @GetMapping()
    public Mono<List<TeamMatchResponseDto>> getRecentMatches() {
        return matchService.getRecentMatches();
    }

    @GetMapping("/all")
    public Mono<List<TeamMatchResponseDto>> getAllMatches() {
        return matchService.getAllMatches();
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public Mono<TeamMatchResponseDto> newMatch(@RequestBody TeamMatchRequestDto requestDto, @AuthenticationPrincipal Jwt jwt) {
        String username = jwt.getClaim("preferred_username");
        return matchService.newMatch(requestDto,username);
    }

    @PreAuthorize("hasRole('admin') or @matchSecurity.isMatchOwner(#id, authentication)")
    @DeleteMapping("/delete/{id}")
    public void deleteMatchById(@PathVariable Long id) {
        matchDeleteService.deleteMatchById(id);
    }

    @PreAuthorize("hasRole('admin') or @matchSecurity.isMatchOwner(#id, authentication)")
    @PutMapping("/update/{id}")
    public void updateMatchById(@PathVariable Long id,@RequestBody TeamMatchRequestDto requestDto) {
        matchUpdateService.updateMatchById(id,requestDto);
    }

    @PreAuthorize("hasRole('admin') or @matchSecurity.isSoloMatchOwner(#id, authentication)")
    @DeleteMapping("solo/delete/{id}")
    public void deleteSoloMatchById(@PathVariable Long id) {
        matchDeleteService.deleteSoloMatchById(id);
    }

    @PreAuthorize("hasRole('admin') or @matchSecurity.isSoloMatchOwner(#id, authentication)")
    @PutMapping("/solo/update/{id}")
    public void updateSoloMatchById(@PathVariable Long id,@RequestBody SoloMatchRequestDto requestDto) {
        matchUpdateService.updateSoloMatchById(id,requestDto);
    }
    @GetMapping("/solo/{id}")
    public Mono<SoloMatchResponseDto> getSoloMatchById(@PathVariable Long id) {
        return soloMatchService.getSoloMatchById(id);
    }

    @GetMapping("/solo")
    public Mono<List<SoloMatchResponseDto>> getRecentSoloMatches() {
        return soloMatchService.getRecentMatches();
    }

    @PostMapping("/solo/new")
    @PreAuthorize("isAuthenticated()")
    public Mono<SoloMatchResponseDto> newSoloMatch(@RequestBody SoloMatchRequestDto requestDto, @AuthenticationPrincipal Jwt jwt) {
        String username = jwt.getClaim("preferred_username");
        return soloMatchService.newSoloMatch(requestDto, username);
    }

    @GetMapping("/statistics")
    public Mono<MatchStatisticsDto> matchStatistics() {
        return matchService.getStatistics();
    }
    @GetMapping("/solo/statistics")
    public Mono<MatchStatisticsDto> matchSoloStatistics() {
        return soloMatchService.getSoloStatistics();
    }

}
