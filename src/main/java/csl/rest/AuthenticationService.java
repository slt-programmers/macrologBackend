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
import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDateTime;
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
        String hashedPassword = DigestUtils.sha256Hex(request.getPassword());
        LOGGER.info("Login attempt: " + username);

        UserAccount userAccount = USER_ACCCOUNT_REPOSITORY.getUser(username);
        if (userAccount == null) {
            userAccount = USER_ACCCOUNT_REPOSITORY.getUserByEmail(username);
        }
        if (userAccount == null) {
            LOGGER.error("Not found");
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        } else if (!checkValidPassword(hashedPassword, userAccount)) {
            LOGGER.error("Unautorized");
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        } else {
            String name = SETTINGS_REPOSITORY.getSetting((int) userAccount.getId(), "name");
            if (name == null) {
                name = username;
            }
            String jwt = Jwts.builder()
                    .setSubject("users/TzMUocMF4p")
                    .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                    .claim("name", name)
                    .claim("userId", userAccount.getId())
                    .signWith(
                            SignatureAlgorithm.HS256,
                            SECRET.getBytes(StandardCharsets.UTF_8)
                    )
                    .compact();
            MultiValueMap<String, String> responseHeaders = new HttpHeaders();
            responseHeaders.add("token", jwt);
            LOGGER.info("Login successful");
            return new ResponseEntity<>("{\"name\":\"" + name + "\", \"token\":\"" + jwt + "\"}", responseHeaders, HttpStatus.ACCEPTED);
        }
    }

    private boolean checkValidPassword(String hashedPassword, UserAccount account) {
        boolean activePasswordOK = account.getPassword().equals(hashedPassword);
        if (!activePasswordOK) {
            boolean resettedPasswordOK = account.getResetpassword() != null &&
                    account.getResetpassword().equals(hashedPassword);

            boolean withinTimeFrame = account.getResetdate() != null &&
                    account.getResetdate().isAfter(LocalDateTime.now().minusMinutes(30));

            if (resettedPasswordOK && withinTimeFrame) {
                LOGGER.info("Password has been reset to verified new password");
                USER_ACCCOUNT_REPOSITORY.updatePassword(account.getUsername(), hashedPassword, null, null);
                return true;
            } else {
                return false;
            }
        }
        return true;
    }

    @RequestMapping(value = "/signup",
            method = POST,
            headers = {"Content-Type=application/json"})
    public ResponseEntity signUp(@RequestBody AuthenticationRequest request) {
        LOGGER.info(request.getEmail());
        String username = request.getUsername();
        String hashedPassword = DigestUtils.sha256Hex(request.getPassword());
        String email = request.getEmail();
        LOGGER.info("Add user attempt: " + username);

        UserAccount account = USER_ACCCOUNT_REPOSITORY.getUser(username);
        if (account != null) {
            return ResponseEntity.status(401).body("Username or email already in use");
        } else {
            UserAccount userByEmail = USER_ACCCOUNT_REPOSITORY.getUserByEmail(email);
            if (userByEmail != null) {
                return ResponseEntity.status(401).body("Username or email already in use");
            } else {
                USER_ACCCOUNT_REPOSITORY.insertUser(username, hashedPassword, email);
                account = USER_ACCCOUNT_REPOSITORY.getUser(username);
            }
        }

        UserAccount newAccount = account;

        String jwt = Jwts.builder()
                .setSubject("users/TzMUocMF4p")
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .claim("name", username)
                .claim("userId", newAccount.getId())
                .signWith(
                        SignatureAlgorithm.HS256,
                        SECRET.getBytes(StandardCharsets.UTF_8)
                )
                .compact();
        MultiValueMap<String, String> responseHeaders = new HttpHeaders();
        responseHeaders.add("token", jwt);
        LOGGER.info("Signup successful");

        new Thread(() -> MailService.sendConfirmationMail(email, newAccount)).start();

        return new ResponseEntity<>("{\"name\":\"" + username + "\", \"token\":\"" + jwt + "\"}", responseHeaders, HttpStatus.ACCEPTED);
    }

    @RequestMapping(value = "/resetPassword",
            method = POST,
            headers = {"Content-Type=application/json"})
    public ResponseEntity resetPassword(@RequestBody AuthenticationRequest request) {
        LOGGER.info("Reset email");
        String email = request.getEmail();
        UserAccount account = USER_ACCCOUNT_REPOSITORY.getUserByEmail(email);
        if (account != null) {
            String randomPassword = RandomStringUtils.randomAlphabetic(10);
            String hashedRandomPassword = DigestUtils.sha256Hex(randomPassword);

            USER_ACCCOUNT_REPOSITORY.updatePassword(account.getUsername(), account.getPassword(), hashedRandomPassword, LocalDateTime.now());
            new Thread(() -> MailService.sendPasswordRetrievalMail(email, randomPassword, account)).start();
            return ResponseEntity.ok("Email matches");
        } else {
            LOGGER.error("Account is null");
            return ResponseEntity.status(404).body("Email not found");
        }
    }

    @RequestMapping(value = "/changePassword",
            method = POST)
    public ResponseEntity changePassword(@RequestBody ChangePasswordRequest request) {
        String oldPasswordHashed = DigestUtils.sha256Hex(request.getOldPassword());
        String newPasswordHashed = DigestUtils.sha256Hex(request.getNewPassword());
        String confirmPasswordHashed = DigestUtils.sha256Hex(request.getConfirmPassword());
        UserInfo userInfo = ThreadLocalHolder.getThreadLocal().get();

        LOGGER.info("Update password attempt for userId: " + userInfo.getUserId());

        UserAccount userAccount = USER_ACCCOUNT_REPOSITORY.getUserById(userInfo.getUserId());
        if (userAccount == null) {
            LOGGER.error("Not found");
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        } else if (!userAccount.getPassword().equals(oldPasswordHashed)) {
            LOGGER.error("Old password incorrect");
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        } else {
            if (!newPasswordHashed.equals(confirmPasswordHashed)) {
                LOGGER.error("Passwords do not match");
                return new ResponseEntity<>("Passwords do not match", HttpStatus.BAD_REQUEST);
            } else {
                LOGGER.info("Passwords match");
                USER_ACCCOUNT_REPOSITORY.updatePassword(userAccount.getUsername(), newPasswordHashed, null, null);
                return new ResponseEntity<>("OK", HttpStatus.OK);
            }
        }
    }

}
