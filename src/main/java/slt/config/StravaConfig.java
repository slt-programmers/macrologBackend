package slt.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Slf4j
@Data
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties("strava")
public class StravaConfig {


    Integer clientId;
    String clientSecret;


    @PostConstruct
    public void configGeladen() {
        log.info("Strava geladen : {}", StringUtils.isNotEmpty(clientSecret));
    }
}
