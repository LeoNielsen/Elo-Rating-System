package EloRatingSystem.Exception;


import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApiException extends Exception {

    private final HttpStatus httpStatus;
    public ApiException(String massage, HttpStatus httpStatus){
        super(massage);
        this.httpStatus = httpStatus;
    }
}
