package slt;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import slt.config.DatabaseConfig;

@SpringBootApplication
@Slf4j
@EnableAutoConfiguration
@AutoConfigureBefore(DatabaseConfig.class)
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
        log.info("Application is now running.");
    }

}
