package slt.rest;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import slt.database.UserAccountRepository;
import slt.database.entities.UserAccount;
import slt.dto.LogEntryDto;
import slt.dto.UserAccountDto;
import slt.security.ThreadLocalHolder;
import slt.security.UserInfo;

import java.util.ArrayList;
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
            List<UserAccountDto> userAccountDtos = mapToDtos(userAccounts);
            return ResponseEntity.ok(userAccountDtos);
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

    private List<UserAccountDto> mapToDtos(List<UserAccount> accounts) {
        List<UserAccountDto> dtos = new ArrayList<>();
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
