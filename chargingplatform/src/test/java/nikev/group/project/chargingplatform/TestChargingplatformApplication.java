package nikev.group.project.chargingplatform;

import org.springframework.boot.SpringApplication;

public class TestChargingplatformApplication {

	public static void main(String[] args) {
		SpringApplication.from(ChargingplatformApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
