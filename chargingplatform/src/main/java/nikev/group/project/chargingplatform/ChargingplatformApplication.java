package nikev.group.project.chargingplatform;

import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ChargingplatformApplication {
	static {
		TimeZone.setDefault(TimeZone.getTimeZone("Europe/Lisbon"));
	}

	public static void main(String[] args) {
		SpringApplication.run(ChargingplatformApplication.class, args);
	}

}
