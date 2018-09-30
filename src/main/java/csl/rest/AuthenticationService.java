package csl.rest;

import csl.database.SettingsRepository;
import csl.database.UserAcccountRepository;
import csl.database.model.UserAccount;
import csl.dto.AuthenticationRequest;
import csl.dto.ChangePasswordRequest;
import csl.notification.MailService;
import csl.security.ThreadLocalHolder;
import csl.security.UserInfo;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
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
    private final static SettingsRepository SETTINGS_REPOSITORY = new SettingsRepository();
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationService.class);

    @RequestMapping(value = "/authenticate",
            method = POST)
    public ResponseEntity authenticateUser(@RequestBody AuthenticationRequest request) {
        String username = request.getUsername();
        String password = request.getPassword();
        LOGGER.info("Login attempt: " + username);

        UserAccount userAccount = USER_ACCCOUNT_REPOSITORY.getUser(username);
        if (userAccount == null) {
            // Maybe the user signed in with his/her email:
            userAccount = USER_ACCCOUNT_REPOSITORY.getUserByEmail(username);
        }
        if (userAccount == null) {
            LOGGER.error("Not found");
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        } else if (!userAccount.getPassword().equals(password)) {
            LOGGER.error("Unautorized");
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        } else {
            String name = SETTINGS_REPOSITORY.getSetting((int) userAccount.getId(), "name");
            if (name == null) {
                name = username;
            }
            try {
                String jwt = Jwts.builder()
                        .setSubject("users/TzMUocMF4p")
                        .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                        .claim("name", name)
                        .claim("userId", userAccount.getId())
//                        .claim("scope", "self groups/admins")
                        .signWith(
                                SignatureAlgorithm.HS256,
                                SECRET.getBytes("UTF-8")
                        )
                        .compact();
                MultiValueMap<String, String> responseHeaders = new HttpHeaders();
                responseHeaders.add("token", jwt);
                LOGGER.info("Login successful");
                return new ResponseEntity<>("{\"name\":\"" + name + "\", \"token\":\"" + jwt + "\"}", responseHeaders, HttpStatus.ACCEPTED);
            } catch (UnsupportedEncodingException e) {
                LOGGER.error(e.getMessage());
                return ResponseEntity.status(500).body(e.getMessage());
            }
        }
    }

    @RequestMapping(value = "/signup",
            method = POST,
            headers = {"Content-Type=application/json"})
    public ResponseEntity signUp(@RequestBody AuthenticationRequest request) {
        LOGGER.info(request.getEmail());
        String username = request.getUsername();
        String password = request.getPassword();
        String email = request.getEmail();
        LOGGER.info("Add user attempt:" + username + " - " + password);

        String encodedPassword = password; // todo = encode

        UserAccount account = USER_ACCCOUNT_REPOSITORY.getUser(username);
        if (account != null) {
            return ResponseEntity.status(401).body("Username already in use");
        } else {
            UserAccount userByEmail = USER_ACCCOUNT_REPOSITORY.getUserByEmail(email);
            if (userByEmail != null){
                return ResponseEntity.status(401).body("Email already in use");
            } else {
                USER_ACCCOUNT_REPOSITORY.insertUser(username, encodedPassword, email);
                account = USER_ACCCOUNT_REPOSITORY.getUser(username);
            }
        }

        UserAccount newAccount = account;
        try {
            String jwt = Jwts.builder()
                    .setSubject("users/TzMUocMF4p")
                    .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                    .claim("name", username)
                    .claim("userId", newAccount.getId())
                    .signWith(
                            SignatureAlgorithm.HS256,
                            SECRET.getBytes("UTF-8")
                    )
                    .compact();
            MultiValueMap<String, String> responseHeaders = new HttpHeaders();
            responseHeaders.add("token", jwt);
            LOGGER.info("Signup successful");

            new Thread() {
                public void run () {
                    MailService.sendConfirmationMail(email, newAccount);
                }
            }.start();

            return new ResponseEntity<>("{\"name\":\"" + username + "\", \"token\":\"" + jwt + "\"}", responseHeaders, HttpStatus.ACCEPTED);
        } catch (UnsupportedEncodingException e) {
            LOGGER.error(e.getMessage());
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @RequestMapping(value = "/validate",
            method = POST,
            headers = {"Content-Type=application/json"})
    public ResponseEntity validate(@RequestBody AuthenticationRequest request) {
        LOGGER.info("Validate email");
        String email = request.getEmail();
        UserAccount account = USER_ACCCOUNT_REPOSITORY.getUserByEmail(email);
        if (account != null) {
            if (account.getEmail().equals(email)) {
                new Thread() {
                    public void run () {
                        MailService.sendPasswordRetrievalMail(email, account);
                    }
                }.start();
                return ResponseEntity.ok("Email matches");
            } else {
                LOGGER.error("No match");
                return ResponseEntity.status(401).body("Email does not match");
            }
        } else {
            LOGGER.error("Account is null");
            return ResponseEntity.status(404).body("Username not found");
        }
    }

    @RequestMapping(value = "/changePassword",
            method = POST)
    public ResponseEntity changePassword(@RequestBody ChangePasswordRequest request) {
        String oldPassword = request.getOldPassword();
        String newPassword = request.getNewPassword();
        String confirmPassword = request.getConfirmPassword();
        UserInfo userInfo = ThreadLocalHolder.getThreadLocal().get();

        LOGGER.info("Update password attempt for userid " + userInfo.getUserId());

        UserAccount userAccount = USER_ACCCOUNT_REPOSITORY.getUserById(userInfo.getUserId());
        if (userAccount == null) {
            LOGGER.error("Not found");
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        } else if (!userAccount.getPassword().equals(oldPassword)) {
            LOGGER.error("Old password incorrect");
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        } else {
            if (!newPassword.equals(confirmPassword)){
                LOGGER.error("passwords do not match");
                return new ResponseEntity("passwords do not match",HttpStatus.BAD_REQUEST);
            } else {
                LOGGER.error("passwords match");
                USER_ACCCOUNT_REPOSITORY.updatePassword(userAccount.getUsername(),newPassword);
                return new ResponseEntity("ok",HttpStatus.OK);

            }
        }
    }

}
