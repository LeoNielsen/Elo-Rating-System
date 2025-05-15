package EloRatingSystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EloRatingSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(EloRatingSystemApplication.class, args);
	}

}
