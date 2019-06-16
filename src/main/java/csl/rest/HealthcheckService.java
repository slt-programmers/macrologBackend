package csl.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
@RequestMapping("/healthcheck")
@Api(value = "healthcheck")
public class HealthcheckService {

    @ApiOperation(value = "Is healthy?")
    @RequestMapping(value = "",
            method = GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity isHealthy() {
        boolean healthy = true;
        return ResponseEntity.ok(healthy);
    }

}
