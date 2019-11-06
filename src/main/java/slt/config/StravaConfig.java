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

    private Integer clientId;
    private String clientSecret;
    private String verifytoken;
    private String callbackUrl;

    @PostConstruct
    public void configLoaded() {
        log.info("Strava configured : {}", StringUtils.isNotEmpty(clientSecret));
    }
}
