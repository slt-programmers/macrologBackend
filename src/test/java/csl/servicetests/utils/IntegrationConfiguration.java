package csl.servicetests.utils;

import csl.notification.MailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Profile("test")
@Configuration
@Slf4j
public class IntegrationConfiguration {

    @Bean
    @Primary
    public MailService mailService() {
        log.debug("Creating mock mail service");
        return new MyMockedMailService(null);
    }
}