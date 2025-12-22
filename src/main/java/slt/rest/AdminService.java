package slt.rest;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import slt.database.UserAccountRepository;
import slt.database.entities.UserAccount;
import slt.dto.ConnectivityRequestDto;
import slt.dto.ConnectivityStatusDto;
import slt.dto.MailDto;
import slt.dto.UserAccountDto;
import slt.security.ThreadLocalHolder;
import slt.service.GoogleMailService;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/admin")
@Slf4j
public class AdminService {

    @Autowired
    private AccountService accountService;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private GoogleMailService mailService;

    @Autowired
    private GoogleMailService notificationMails;


    @GetMapping(path = "/getAllUsers", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UserAccountDto>> getAllUsers() {
        final var userInfo = ThreadLocalHolder.getThreadLocal().get();
        final var userId = userInfo.getUserId();
        final var userAccount = userAccountRepository.getUserById(userId);
        if (!userAccount.isAdmin()) {
            log.error("Not authorized to get all users");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } else {
            List<UserAccount> userAccounts = userAccountRepository.getAllUsers();
            List<UserAccountDto> userAccountDtos = mapToDtos(userAccounts);
            return ResponseEntity.ok(userAccountDtos);
        }
    }

    @PostMapping(path = "/deleteAccount", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> deleteAccount(@RequestParam("userId") Long deleteUserId) {
        final var userInfo = ThreadLocalHolder.getThreadLocal().get();
        final var userId = userInfo.getUserId();
        final var userAccount = userAccountRepository.getUserById(userId);
        final var toBeDeletedAccount = userAccountRepository.getUserById(deleteUserId);
        if (!userAccount.isAdmin()) {
            log.error("Not authorized to delete account");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } else if (toBeDeletedAccount == null) {
            log.error("Account not found for userId: {}", userInfo.getUserId());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } else if (toBeDeletedAccount.isAdmin()){
            log.error("Cannot delete admin account");
            return  ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } else {
            accountService.deleteAccount(deleteUserId);
            return ResponseEntity.status(HttpStatus.OK).build();
        }
    }

    @GetMapping(path = "/mail/status", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ConnectivityStatusDto> getMailStatus() {
        final var userInfo = ThreadLocalHolder.getThreadLocal().get();
        final var userId = userInfo.getUserId();
        final var userAccount = userAccountRepository.getUserById(userId);
        if (!userAccount.isAdmin()) {
            log.error("Not authorized to get mail config");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } else {
            return ResponseEntity.ok(mailService.getMailStatus());
        }
    }

    @PostMapping(path = "/mail", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> storeMailSetting(@RequestBody ConnectivityRequestDto connectivityRequestDto) {
        final var userInfo = ThreadLocalHolder.getThreadLocal().get();
        final var userId = userInfo.getUserId();
        final var userAccount = userAccountRepository.getUserById(userId);
        if (!userAccount.isAdmin()) {
            log.error("Not authorized to store mail settings");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } else {
            log.debug("Handling connectivity request ");
            mailService.registerWithCode(connectivityRequestDto.getClientAuthorizationCode());
            return ResponseEntity.ok().build();
        }
    }
    @PostMapping(path = "/mail/testmail", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> sendTestMail(@RequestBody MailDto mailRequest) {
        final var userInfo = ThreadLocalHolder.getThreadLocal().get();
        final var userId = userInfo.getUserId();
        final var userAccount = userAccountRepository.getUserById(userId);
        if (!userAccount.isAdmin()) {
            log.error("Not authorized to send testmail");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } else {
            log.debug("Sending mail to  {}", mailRequest.getEmailTo());
            mailService.sendTestMail(mailRequest.getEmailTo());
            return ResponseEntity.ok().build();
        }
    }

    private List<UserAccountDto> mapToDtos(List<UserAccount> accounts) {
        final var dtos = new ArrayList<UserAccountDto>();
        for (UserAccount account : accounts) {
            dtos.add(mapToDto(account));
        }
        return dtos;
    }

    private UserAccountDto mapToDto(UserAccount account) {
        return new UserAccountDto(
                account.getId(),
                null,
                account.getUsername(),
                account.getEmail(),
                account.isAdmin());
    }
}
