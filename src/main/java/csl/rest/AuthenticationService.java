package csl.rest;

import csl.database.FoodRepository;
import csl.database.LogEntryRepository;
import csl.database.PortionRepository;
import csl.database.SettingsRepository;
import csl.database.model.Food;
import csl.database.model.Setting;
import csl.dto.*;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
@RequestMapping("/api")
public class AuthenticationService {


    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationService.class);

    @CrossOrigin(origins = "http://localhost:4200")
    @RequestMapping(value = "/authenticate",
            method = POST,
            headers = {"Content-Type=application/json"})
    public ResponseEntity addFood(@RequestBody AuthenticationRequest request) {
        LOGGER.error("Login attempt:" + request.getUsername() + " - " + request.getPassword());


        try {
            String jwt = Jwts.builder()
                    .setSubject("users/TzMUocMF4p")
                   // .setExpiration(new Date(1300819380))
                    .claim("name", "Macro Log")
                    .claim("scope", "self groups/admins")
                    .signWith(
                            SignatureAlgorithm.HS256,
                            "secret".getBytes("UTF-8")
                    )
                    .compact();
            MultiValueMap<String, String> responseHeaders = new HttpHeaders();
            responseHeaders.add("token",jwt);
            return new ResponseEntity("200",responseHeaders, HttpStatus.ACCEPTED);

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return ResponseEntity.ok("ok");

    }
}
