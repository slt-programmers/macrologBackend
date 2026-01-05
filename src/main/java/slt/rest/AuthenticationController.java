package slt.rest;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import slt.dto.*;
import slt.security.ThreadLocalHolder;
import slt.service.AccountService;
import slt.util.PasswordUtils;

@Slf4j
@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class AuthenticationController {

    private AccountService accountService;

    @PostMapping(path = "/authenticate")
    public ResponseEntity<UserAccountDto> authenticateUser(@RequestBody final AuthenticationRequest request) {
        final var username = request.getUsername();
        log.info("Attempt to login for user [{}] ", username);
        final var hashedPassword = PasswordUtils.hashPassword(request.getPassword());
        final var userAccountDto = accountService.getAccount(username, hashedPassword);
        final var responseHeaders = new HttpHeaders();
        responseHeaders.add("token", userAccountDto.getToken());
        log.info("Login successful");
        return new ResponseEntity<>(userAccountDto, responseHeaders, HttpStatus.ACCEPTED);
    }

    @PostMapping(path = "/signup")
    public ResponseEntity<UserAccountDto> signUp(@RequestBody final RegistrationRequest request) {
        final var username = request.getUsername();
        log.info("Attempt to register user [{}]", username);
        final var email = request.getEmail();
        final var hashedPassword = PasswordUtils.hashPassword(request.getPassword());
        final var userDto = accountService.registerAccount(username, email, hashedPassword);
        final var responseHeaders = new HttpHeaders();
        responseHeaders.add("token", userDto.getToken());
        log.info("Registration successful");
        return new ResponseEntity<>(userDto, responseHeaders, HttpStatus.ACCEPTED);
    }

    @PostMapping(path = "/resetPassword")
    public ResponseEntity<Void> resetPassword(@RequestBody final ResetPasswordRequest request) {
        accountService.sendPasswordRetrievalMail(request.getEmail());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping(path = "/changePassword")
    public ResponseEntity<Void> changePassword(@RequestBody final ChangePasswordRequest request) {
        final var userInfo = ThreadLocalHolder.getThreadLocal().get();
        log.info("Attempt to update password for userId [{}]", userInfo.getUserId());
        final var oldPasswordHashed = PasswordUtils.hashPassword(request.getOldPassword());
        final var newPasswordHashed = PasswordUtils.hashPassword(request.getNewPassword());
        final var confirmPasswordHashed = PasswordUtils.hashPassword(request.getConfirmPassword());
        accountService.changePassword(userInfo.getUserId(), oldPasswordHashed, newPasswordHashed, confirmPasswordHashed);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping(path = "/deleteAccount")
    public ResponseEntity<Void> deleteAccount(@RequestParam("password") final String password) {
        final var userInfo = ThreadLocalHolder.getThreadLocal().get();
        final var passwordHashed = PasswordUtils.hashPassword(password);
        accountService.deleteAccount(userInfo.getUserId(), passwordHashed);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
