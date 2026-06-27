package EloRatingSystem.Modules.Team.Services;

import EloRatingSystem.Exception.ApiException;
import EloRatingSystem.Modules.Team.Dtos.TeamPairResponseDto;
import EloRatingSystem.Modules.Team.Dtos.TeamResponseDto;
import EloRatingSystem.Modules.Team.Models.Team;
import EloRatingSystem.Modules.Team.Models.TeamPair;
import EloRatingSystem.Modules.Team.Repositories.TeamPairRepository;
import EloRatingSystem.Modules.Team.Repositories.TeamRepository;
import EloRatingSystem.Modules.player.Models.Player;
import EloRatingSystem.Modules.player.Repositories.PlayerRepository;
import jakarta.transaction.Transactional;
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
    @Autowired
    TeamPairRepository teamPairRepository;


    public Team getTeam(long atkId, long defId) throws ApiException {

        Player attacker = playerRepository.findById(atkId)
                .orElseThrow(() -> new ApiException(
                        String.format("player %s doesn't exist", atkId),
                        HttpStatus.BAD_REQUEST));
        Player defender = playerRepository.findById(defId)
                .orElseThrow(() -> new ApiException(
                        String.format("player %s doesn't exist", defId),
                        HttpStatus.BAD_REQUEST));

        long playerAId = Math.min(atkId, defId);
        long playerBId = Math.max(atkId, defId);

        TeamPair pair =
                teamPairRepository.findByPlayerAIdAndPlayerBId(playerAId, playerBId).orElseGet(() -> {

                    Player playerA = (playerAId == atkId) ? attacker : defender;
                    Player playerB = (playerBId == defId) ? defender : attacker;
                    return teamPairRepository.save(new TeamPair(playerA,playerB,1200));
                });


        Optional<Team> teamOptional = teamRepository.findByAttackerIdAndDefenderId(atkId, defId);
        if (teamOptional.isPresent()) {
            Team team = teamOptional.get();
            if (team.getPair() == null) {
                team.setPair(pair);
                return teamRepository.save(team);
            }
            return team;
        } else {
            Team team = new Team(attacker, defender,pair);
            return teamRepository.save(team);
        }
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

    public  Mono<List<TeamPairResponseDto>> getAllTeamPair(){
        List<TeamPair> pairs = teamPairRepository.findAll();

        List<TeamPairResponseDto> pairResponseDtos = new ArrayList<>();
        for(TeamPair t: pairs){
            if(!t.getPlayerA().getActive() && !t.getPlayerB().getActive())
                continue;
            pairResponseDtos.add(new TeamPairResponseDto(t));
        }
        return Mono.just(pairResponseDtos);
    }


    @Transactional
    public void backfillTeamPairs() {

        List<Team> allTeams = teamRepository.findAll();

        for (Team team : allTeams) {

            long atkId = team.getAttacker().getId();
            long defId = team.getDefender().getId();

            long playerAId = Math.min(atkId, defId);
            long playerBId = Math.max(atkId, defId);

            Player playerA = (playerAId == atkId) ? team.getAttacker() : team.getDefender();
            Player playerB = (playerBId == defId) ? team.getDefender() : team.getAttacker();

            TeamPair pair = teamPairRepository
                    .findByPlayerAIdAndPlayerBId(playerAId, playerBId)
                    .orElseGet(() -> teamPairRepository.save(new TeamPair(playerA, playerB, 1200)));

            if (team.getPair() == null) {
                team.setPair(pair);
                teamRepository.save(team);
            }
        }
    }
}
