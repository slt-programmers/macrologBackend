package csl;

import csl.config.DatabaseConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
@EnableAutoConfiguration
@AutoConfigureBefore(DatabaseConfig.class)
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, null);
        log.info("Application is now running.");
    }

}
