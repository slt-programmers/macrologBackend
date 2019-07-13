package slt.rest;

import slt.database.SettingsRepository;
import slt.database.UserAcccountRepository;
import slt.database.model.Setting;
import slt.database.model.UserAccount;
import slt.dto.AuthenticationRequest;
import slt.dto.RegistrationRequest;
import slt.dto.ChangePasswordRequest;
import slt.dto.ResetPasswordRequest;
import slt.notification.MailService;
import slt.security.ThreadLocalHolder;
import slt.security.UserInfo;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Date;

import static slt.security.SecurityConstants.EXPIRATION_TIME;
import static slt.security.SecurityConstants.SECRET;

@RestController
@RequestMapping("/api")
@Slf4j
public class AuthenticationService {

    @Autowired
    private UserAcccountRepository userAcccountRepository;

    @Autowired
    private SettingsRepository settingsRepository;

    @Autowired
    private MailService mailService;

    @Autowired
    private AccountService accountService;

    @PostMapping(path = "/authenticate", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity authenticateUser(@RequestBody AuthenticationRequest request) {
        String username = request.getUsername();
        String hashedPassword = DigestUtils.sha256Hex(request.getPassword());
        log.info("Login attempt {} ", username);

        UserAccount userAccount = userAcccountRepository.getUser(username);
        if (userAccount == null) {
            userAccount = userAcccountRepository.getUserByEmail(username);
        }
        if (userAccount == null) {
            log.error("Not found");
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        } else if (!checkValidPassword(hashedPassword, userAccount)) {
            log.error("Unautorized");
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
            log.info("Login successful");
            return new ResponseEntity<>("{\"name\":\"" + name + "\", \"token\":\"" + jwt + "\"}", responseHeaders, HttpStatus.ACCEPTED);
        }
    }

    @PostMapping(path = "/signup", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity signUp(@RequestBody RegistrationRequest request) {
        log.info(request.getEmail());
        String username = request.getUsername();
        String hashedPassword = DigestUtils.sha256Hex(request.getPassword());
        String email = request.getEmail();
        log.info("Add user attempt: {}", username);

        UserAccount account = userAcccountRepository.getUser(username);
        if (account != null) {
            log.debug("Username of email already in use 1");
            return ResponseEntity.status(401).body("Username or email already in use");
        } else {
            UserAccount userByEmail = userAcccountRepository.getUserByEmail(email);
            if (userByEmail != null) {
                log.debug("Username or email already in use 2");
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
        log.info("Signup successful");

        new Thread(() -> mailService.sendConfirmationMail(email, newAccount)).start();

        return new ResponseEntity<>("{\"name\":\"" + username + "\", \"token\":\"" + jwt + "\"}", responseHeaders, HttpStatus.ACCEPTED);
    }

    @PostMapping(path = "/resetPassword", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity resetPassword(@RequestBody ResetPasswordRequest request) {
        log.info("Reset email");
        String email = request.getEmail();
        UserAccount account = userAcccountRepository.getUserByEmail(email);
        if (account != null) {
            String randomPassword = RandomStringUtils.randomAlphabetic(10);
            String hashedRandomPassword = DigestUtils.sha256Hex(randomPassword);

            userAcccountRepository.updatePassword(account.getUsername(), account.getPassword(), hashedRandomPassword, LocalDateTime.now());
            new Thread(() -> mailService.sendPasswordRetrievalMail(email, randomPassword, account)).start();
            return ResponseEntity.ok("Email matches");
        } else {
            log.error("Account is null");
            return ResponseEntity.status(404).body("Email not found");
        }
    }

    @PostMapping(path = "/changePassword", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity changePassword(@RequestBody ChangePasswordRequest request) {
        String oldPasswordHashed = DigestUtils.sha256Hex(request.getOldPassword());
        String newPasswordHashed = DigestUtils.sha256Hex(request.getNewPassword());
        String confirmPasswordHashed = DigestUtils.sha256Hex(request.getConfirmPassword());
        UserInfo userInfo = ThreadLocalHolder.getThreadLocal().get();

        log.info("Update password attempt for userId: {}", userInfo.getUserId());

        UserAccount userAccount = userAcccountRepository.getUserById(userInfo.getUserId());
        if (userAccount == null) {
            log.error("Not found");
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        } else if (!userAccount.getPassword().equals(oldPasswordHashed)) {
            log.error("Old password incorrect");
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        } else {
            if (!newPasswordHashed.equals(confirmPasswordHashed)) {
                log.error("Passwords do not match");
                return new ResponseEntity<>("Passwords do not match", HttpStatus.BAD_REQUEST);
            } else {
                log.info("Passwords match");
                userAcccountRepository.updatePassword(userAccount.getUsername(), newPasswordHashed, null, null);
                return new ResponseEntity<>("OK", HttpStatus.OK);
            }
        }
    }

    @PostMapping(path = "/deleteAccount", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity deleteAccount(@RequestParam("password") String password) {
        String passwordHashed = DigestUtils.sha256Hex(password);
        UserInfo userInfo = ThreadLocalHolder.getThreadLocal().get();
        Integer userId = userInfo.getUserId();
        UserAccount userAccount = userAcccountRepository.getUserById(userId);
        if (userAccount == null) {
            log.error("Account not found for userId: {}", userInfo.getUserId());
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        } else if (!userAccount.getPassword().equals(passwordHashed)) {
            log.error("Could not delete account: password incorrect");
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        } else {
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
                log.info("Password has been reset to verified new password");
                userAcccountRepository.updatePassword(account.getUsername(), hashedPassword, null, null);
                return true;
            } else {
                return false;
            }
        }
        return true;
    }

}
