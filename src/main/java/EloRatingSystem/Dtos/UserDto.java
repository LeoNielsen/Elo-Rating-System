package EloRatingSystem.Dtos;

import EloRatingSystem.Models.User;
import EloRatingSystem.UserRoles.Role;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserDto {
    private String username;
    private Role role;

    public UserDto(User user){
        this.username = user.getUsername();
        this.role = user.getRole();
    }

}