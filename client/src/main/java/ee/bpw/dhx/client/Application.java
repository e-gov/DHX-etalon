package ee.bpw.dhx.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.vaadin.ui.TextArea;

@SpringBootApplication
@EnableScheduling
@ComponentScan(basePackages="ee.bpw.dhx.*")
public class Application extends SpringBootServletInitializer{

	 @Override
	    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
	        return application.sources(Application.class);
	    }
	 
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}