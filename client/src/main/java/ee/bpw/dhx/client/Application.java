package ee.bpw.dhx.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.vaadin.ui.TextArea;

@SpringBootApplication
@EnableScheduling
@ComponentScan(basePackages="ee.bpw.dhx.*")
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}
