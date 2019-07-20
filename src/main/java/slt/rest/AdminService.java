package slt.rest;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import slt.database.UserAccountRepository;
import slt.database.entities.UserAccount;
import slt.security.ThreadLocalHolder;
import slt.security.UserInfo;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin")
@Slf4j
public class AdminService {

    @Autowired
    private AccountService accountService;

    @Autowired
    private UserAccountRepository userAccountRepository;


    @GetMapping(path = "/getAllUsers", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getAllUsers() {
        UserInfo userInfo = ThreadLocalHolder.getThreadLocal().get();
        Integer userId = userInfo.getUserId();
        UserAccount userAccount = userAccountRepository.getUserById(userId);
        if (!userAccount.isAdmin()) {
            log.error("Not authorized to delete account");
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        } else {
            List<UserAccount> userAccounts = userAccountRepository.getAllUsers();
            userAccounts = userAccounts.stream().filter(acc -> !acc.getId().equals(userId)).collect(Collectors.toList());
            return ResponseEntity.ok(userAccounts);
        }
    }

    @PostMapping(path = "/deleteAccount", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity deleteAccount(@RequestParam("userId") Integer deleteUserId) {
        UserInfo userInfo = ThreadLocalHolder.getThreadLocal().get();
        Integer userId = userInfo.getUserId();
        UserAccount userAccount = userAccountRepository.getUserById(userId);
        UserAccount toBeDeletedAccount = userAccountRepository.getUserById(deleteUserId);
        if (!userAccount.isAdmin()) {
            log.error("Not authorized to delete account");
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        } else if (toBeDeletedAccount == null) {
            log.error("Account not found for userId: {}", userInfo.getUserId());
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        } else {
            accountService.deleteAccount(deleteUserId);
            return new ResponseEntity(HttpStatus.OK);
        }
    }


}
