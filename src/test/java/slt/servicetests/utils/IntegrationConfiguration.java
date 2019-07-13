package slt.servicetests.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import slt.notification.MailService;

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