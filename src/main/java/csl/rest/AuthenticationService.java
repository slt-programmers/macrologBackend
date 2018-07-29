package csl.rest;

import csl.database.UserAcccountRepository;
import csl.database.model.UserAccount;
import csl.dto.AuthenticationRequest;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;
import java.util.Date;

import static csl.security.SecurityConstants.EXPIRATION_TIME;
import static csl.security.SecurityConstants.SECRET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
@RequestMapping("/api")
public class AuthenticationService {


    private final static UserAcccountRepository USER_ACCCOUNT_REPOSITORY = new UserAcccountRepository();
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationService.class);

    @CrossOrigin(origins = "http://localhost:4200")
    @RequestMapping(value = "/authenticate",
            method = POST,
            headers = {"Content-Type=application/json"})
    public ResponseEntity authenticateUser(@RequestBody AuthenticationRequest request) {
        LOGGER.error("Login attempt:" + request.getUsername() + " - " + request.getPassword());

        UserAccount userAccount = USER_ACCCOUNT_REPOSITORY.getUser(request.getUsername());
        if (userAccount == null || !userAccount.getPassword().equals(request.getPassword())) {
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        } else {

            try {
                String jwt = Jwts.builder()
                        .setSubject("users/TzMUocMF4p")
                        .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
//                        .claim("name", "Macro Log")
                        .claim("userId", userAccount.getId())
//                        .claim("scope", "self groups/admins")
                        .signWith(
                                SignatureAlgorithm.HS256,
                                SECRET.getBytes("UTF-8")
                        )
                        .compact();
                MultiValueMap<String, String> responseHeaders = new HttpHeaders();
                responseHeaders.add("token", jwt);
                return new ResponseEntity("{\"token\":\"" + jwt + "\"}", responseHeaders, HttpStatus.ACCEPTED);

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }


            return ResponseEntity.ok("ok");
        }
    }

    @CrossOrigin(origins = "http://localhost:4200")
    @RequestMapping(value = "/signup",
            method = POST,
            headers = {"Content-Type=application/json"})
    public ResponseEntity signUp(@RequestBody AuthenticationRequest request) {
        LOGGER.error("Add user attempt:" + request.getUsername() + " - " + request.getPassword());

        String encodedPassword = request.getPassword(); // todo = encode
        USER_ACCCOUNT_REPOSITORY.insertUser(request.getUsername(), encodedPassword);

        return ResponseEntity.ok("ok");

    }
}
