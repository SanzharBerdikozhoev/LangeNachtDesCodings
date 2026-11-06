package whz.pti.eva;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

//comment just to check documentaion updates (Zweite Versuch)
@SpringBootApplication
public class PizzaProjectTashbaevKurbanovBerdikozhoevApplication {

	public static void main(String[] args) {
		SpringApplication.run(PizzaProjectTashbaevKurbanovBerdikozhoevApplication.class, args);
	}
	
	@Bean
	CommandLineRunner init() {
		return (evt) -> System.out.println("Hallo Welt");
	}
}
