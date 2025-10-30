package slt;

import com.google.common.collect.Lists;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;

@EnableSwagger2
@Slf4j
public class ApplicationConfig {
    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.basePackage("slt.rest"))
                .paths(PathSelectors.any())
                .build().securitySchemes(Lists.newArrayList(apiKey()))
                .securityContexts(Arrays.asList(securityContext()))
                .apiInfo(metaData());
    }

    @PostConstruct
    void started() {
        log.debug("Timezone of server [{}]",TimeZone.getDefault().getDisplayName());
    }

    @Bean
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }

    private ApiInfo metaData() {
        return new ApiInfoBuilder()
                .title("Macrolog Backend REST API")
                .description("\"Spring Boot REST API for the Macrolog application\"")
                .version("1.0.0")
                .contact(new Contact("Carmen Scholte Lubberink en Arjan Tienkamp", "http://carmenscholte.com", "arjan.tienkamp@gmail.com;carmenscholtelubberink@gmail.com"))
                .build();
    }

    private ApiKey apiKey() {
        return new ApiKey("apiKey", "Authorization", "header");
    }

    private SecurityContext securityContext() {
        return SecurityContext.builder().securityReferences(defaultAuth())
                .forPaths(PathSelectors.any()).build();
    }

    private List<SecurityReference> defaultAuth() {
        AuthorizationScope authorizationScope = new AuthorizationScope(
                "global", "accessEverything");
        AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
        authorizationScopes[0] = authorizationScope;
        return Arrays.asList(new SecurityReference("apiKey",
                authorizationScopes));
    }

}