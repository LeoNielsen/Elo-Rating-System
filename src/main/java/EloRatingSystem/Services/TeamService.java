package EloRatingSystem.Services;

import EloRatingSystem.Dtos.TeamRequestDto;
import EloRatingSystem.Dtos.TeamResponseDto;
import EloRatingSystem.Exception.ApiException;
import EloRatingSystem.Models.Player;
import EloRatingSystem.Models.Team;
import EloRatingSystem.Reporitories.PlayerRepository;
import EloRatingSystem.Reporitories.TeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Service
public class TeamService {

    @Autowired
    TeamRepository teamRepository;
    @Autowired
    PlayerRepository playerRepository;

    public Mono<TeamResponseDto> newTeam(TeamRequestDto requestDto) {
        Optional<Player> attackerOptional = playerRepository.findByNameTag(requestDto.getAttacker());
        Optional<Player> defenderOptional = playerRepository.findByNameTag(requestDto.getDefender());
        if (attackerOptional.isPresent() && defenderOptional.isPresent()) {
            Player attacker = attackerOptional.get();
            Player defender = defenderOptional.get();

            Optional<Team> teamOptional = teamRepository.findByAttacker_IdAndDefender_Id(attacker.getId(), defender.getId());
            if (teamOptional.isPresent()) {
                return Mono.just(new TeamResponseDto(teamOptional.get()));
            }

            Team team = teamRepository.save(new Team(attacker, defender));
            return Mono.just(new TeamResponseDto(team));
        }

        return Mono.error(new ApiException(String.format("Either %s or %s does not exit"
                , requestDto.getAttacker(), requestDto.getDefender())
                , HttpStatus.BAD_REQUEST));
    }

    private boolean checkIfTeamExists(Long attackerId, Long defenderId) {
        return teamRepository.findByAttacker_IdAndDefender_Id(attackerId, defenderId).isPresent();
    }
}
