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
@ConfigurationProperties("google")
public class GoogleConfig {


    String clientId;
    String clientSecret;
    String redirectUri;
    String applicationName;


    @PostConstruct
    public void configGeladen() {
        log.info("{} Google geladen : {}", applicationName, StringUtils.isNotEmpty(clientSecret));
    }

}
