package EloRatingSystem.Services;

import EloRatingSystem.Dtos.PlayerRequestDto;
import EloRatingSystem.Dtos.PlayerResponseDto;
import EloRatingSystem.Exception.ApiException;
import EloRatingSystem.Models.Player;
import EloRatingSystem.Reporitories.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class PlayerService {

    @Autowired
    PlayerRepository playerRepository;

    public Mono<PlayerResponseDto> newPlayer(PlayerRequestDto requestDto) {
        if (!checkIfPlayerExists(requestDto.getNameTag())) {
            Player player = new Player(requestDto.getNameTag(), 1200, true);
            playerRepository.save(player);
            return Mono.just(new PlayerResponseDto(player));
        }
        return Mono.error(new ApiException("Name tag already in use", HttpStatus.INTERNAL_SERVER_ERROR));
    }

    private boolean checkIfPlayerExists(String nameTag) {
        return playerRepository.findByNameTag(nameTag).isPresent();
    }


}
