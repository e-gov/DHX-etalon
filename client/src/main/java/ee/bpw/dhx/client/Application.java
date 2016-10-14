package ee.bpw.dhx.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * Class containing main method.
 * 
 * @author Aleksei Kokarev
 *
 */
@SpringBootApplication
@EnableScheduling
@EnableAutoConfiguration
@EnableWebMvc
@ComponentScan(basePackages = "ee.bpw.dhx.client.*,ee.bpw.dhx.ws.config,"
    + "ee.bpw.dhx.ws.endpoint,ee.bpw.dhx.ws.beanconfig,ee.bpw.dhx.ws.schedule")
public class Application extends SpringBootServletInitializer {

  @Override
  protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
    return application.sources(Application.class);
  }

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }
}
