package csl.rest;

import csl.database.SettingsRepository;
import csl.database.UserAcccountRepository;
import csl.database.model.Setting;
import csl.database.model.UserAccount;
import csl.dto.AuthenticationRequest;
import csl.dto.ChangePasswordRequest;
import csl.notification.MailService;
import csl.security.ThreadLocalHolder;
import csl.security.UserInfo;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Date;

import static csl.security.SecurityConstants.EXPIRATION_TIME;
import static csl.security.SecurityConstants.SECRET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
@RequestMapping("/api")
public class AuthenticationService {

    @Autowired
    private UserAcccountRepository userAcccountRepository;

    @Autowired
    private SettingsRepository settingsRepository;

    @Autowired
    private MailService mailService;

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationService.class);

    @RequestMapping(value = "/authenticate",
            method = POST)
    public ResponseEntity authenticateUser(@RequestBody AuthenticationRequest request) {
        String username = request.getUsername();
        String hashedPassword = DigestUtils.sha256Hex(request.getPassword());
        LOGGER.info("Login attempt: " + username);

        UserAccount userAccount = userAcccountRepository.getUser(username);
        if (userAccount == null) {
            userAccount = userAcccountRepository.getUserByEmail(username);
        }
        if (userAccount == null) {
            LOGGER.error("Not found");
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        } else if (!checkValidPassword(hashedPassword, userAccount)) {
            LOGGER.error("Unautorized");
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        } else {
            Setting nameSetting = settingsRepository.getLatestSetting((int) userAccount.getId(), "name");
            String name = username;
            if (nameSetting != null) {
                name = nameSetting.getValue();
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

    @RequestMapping(value = "/signup",
            method = POST,
            headers = {"Content-Type=application/json"})
    public ResponseEntity signUp(@RequestBody AuthenticationRequest request) {
        LOGGER.info(request.getEmail());
        String username = request.getUsername();
        String hashedPassword = DigestUtils.sha256Hex(request.getPassword());
        String email = request.getEmail();
        LOGGER.info("Add user attempt: " + username);

        UserAccount account = userAcccountRepository.getUser(username);
        if (account != null) {
            return ResponseEntity.status(401).body("Username or email already in use");
        } else {
            UserAccount userByEmail = userAcccountRepository.getUserByEmail(email);
            if (userByEmail != null) {
                return ResponseEntity.status(401).body("Username or email already in use");
            } else {
                userAcccountRepository.insertUser(username, hashedPassword, email);
                account = userAcccountRepository.getUser(username);
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

        new Thread(() -> mailService.sendConfirmationMail(email, newAccount)).start();

        return new ResponseEntity<>("{\"name\":\"" + username + "\", \"token\":\"" + jwt + "\"}", responseHeaders, HttpStatus.ACCEPTED);
    }

    @RequestMapping(value = "/resetPassword",
            method = POST,
            headers = {"Content-Type=application/json"})
    public ResponseEntity resetPassword(@RequestBody AuthenticationRequest request) {
        LOGGER.info("Reset email");
        String email = request.getEmail();
        UserAccount account = userAcccountRepository.getUserByEmail(email);
        if (account != null) {
            String randomPassword = RandomStringUtils.randomAlphabetic(10);
            String hashedRandomPassword = DigestUtils.sha256Hex(randomPassword);

            userAcccountRepository.updatePassword(account.getUsername(), account.getPassword(), hashedRandomPassword, LocalDateTime.now());
            new Thread(() -> mailService.sendPasswordRetrievalMail(email, randomPassword, account)).start();
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

        UserAccount userAccount = userAcccountRepository.getUserById(userInfo.getUserId());
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
                userAcccountRepository.updatePassword(userAccount.getUsername(), newPasswordHashed, null, null);
                return new ResponseEntity<>("OK", HttpStatus.OK);
            }
        }
    }

    @RequestMapping(value = "/deleteAccount", method = POST)
    public ResponseEntity deleteAccount(@RequestParam("password") String password) {
        String passwordHashed = DigestUtils.sha256Hex(new String(Base64.getDecoder().decode(password)));
        UserInfo userInfo = ThreadLocalHolder.getThreadLocal().get();
        Integer userId = userInfo.getUserId();
        UserAccount userAccount = userAcccountRepository.getUserById(userId);
        if (userAccount == null) {
            LOGGER.error("Account not found for userId: " + userInfo.getUserId());
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        } else if (!userAccount.getPassword().equals(passwordHashed)) {
            LOGGER.error("Could not delete account: password incorrect");
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        } else {
            AccountService accountService = new AccountService();
            accountService.deleteAccount(userId);
            return new ResponseEntity(HttpStatus.OK);
        }
    }

    private boolean checkValidPassword(String hashedPassword, UserAccount account) {
        boolean activePasswordOK = account.getPassword().equals(hashedPassword);
        if (!activePasswordOK) {
            boolean resettedPasswordOK = account.getResetPassword() != null &&
                    account.getResetPassword().equals(hashedPassword);

            boolean withinTimeFrame = account.getResetDate() != null &&
                    account.getResetDate().isAfter(LocalDateTime.now().minusMinutes(30));

            if (resettedPasswordOK && withinTimeFrame) {
                LOGGER.info("Password has been reset to verified new password");
                userAcccountRepository.updatePassword(account.getUsername(), hashedPassword, null, null);
                return true;
            } else {
                return false;
            }
        }
        return true;
    }

}
