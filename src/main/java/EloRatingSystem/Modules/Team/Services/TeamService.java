package EloRatingSystem.Modules.Team.Services;

import EloRatingSystem.Modules.Team.Dtos.TeamRequestDto;
import EloRatingSystem.Modules.Team.Dtos.TeamResponseDto;
import EloRatingSystem.Exception.ApiException;
import EloRatingSystem.Modules.player.Models.Player;
import EloRatingSystem.Modules.Team.Models.Team;
import EloRatingSystem.Modules.player.Repositories.PlayerRepository;
import EloRatingSystem.Modules.Team.Repositories.TeamRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class TeamService {

    @Autowired
    TeamRepository teamRepository;
    @Autowired
    PlayerRepository playerRepository;

    public Mono<TeamResponseDto> newTeam(TeamRequestDto requestDto) {
        Optional<Player> attackerOptional = playerRepository.findById(requestDto.getAttackerId());
        Optional<Player> defenderOptional = playerRepository.findById(requestDto.getDefenderId());
        if (attackerOptional.isPresent() && defenderOptional.isPresent()) {
            Player attacker = attackerOptional.get();
            Player defender = defenderOptional.get();

            Optional<Team> teamOptional = teamRepository.findByAttackerIdAndDefenderId(attacker.getId(), defender.getId());
            if (teamOptional.isPresent()) {
                return Mono.just(new TeamResponseDto(teamOptional.get()));
            }

            Team team = teamRepository.save(new Team(attacker, defender));
            return Mono.just(new TeamResponseDto(team));
        }

        return Mono.error(new ApiException(String.format("Either %s or %s does not exit"
                , requestDto.getAttackerId(), requestDto.getDefenderId())
                , HttpStatus.BAD_REQUEST));
    }

    public Mono<List<TeamResponseDto>> getAllTeams() {
        List<Team> teams = teamRepository.findAll();

        List<TeamResponseDto> teamResponseDtoList = new ArrayList<>();
        for (Team t:teams) {
            if(!t.getAttacker().getActive() && !t.getDefender().getActive())
                continue;
            teamResponseDtoList.add(new TeamResponseDto(t));
        }
        return Mono.just(teamResponseDtoList);
    }
}
