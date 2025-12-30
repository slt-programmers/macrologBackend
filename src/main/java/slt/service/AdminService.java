package slt.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import slt.database.UserAccountRepository;
import slt.dto.UserAccountDto;
import slt.exceptions.NotFoundException;
import slt.exceptions.UnauthorizedException;
import slt.exceptions.ValidationException;
import slt.mapper.UserAccountMapper;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class AdminService {

    private AccountService accountService;
    private UserAccountRepository userAccountRepository;

    private final UserAccountMapper userAccountMapper = UserAccountMapper.INSTANCE;

    public void verifyAdmin() {
        final var isAdmin = accountService.isAdmin();
        if (!isAdmin) {
            throw new UnauthorizedException("Not authorized as admin");
        }
    }

    public List<UserAccountDto> getAllUsers() {
        final var userAccounts = userAccountRepository.getAllUsers();
        return userAccountMapper.map(userAccounts);
    }

    public void deleteUserAccount(final Long deleteUserId) {
        final var toBeDeletedAccount = userAccountRepository.getUserById(deleteUserId);
        if (toBeDeletedAccount.isEmpty()) {
            log.error("Account not found for userId [{}]", deleteUserId);
            throw new NotFoundException("Account not found.");
        }
        if (toBeDeletedAccount.get().isAdmin()) {
            log.error("Cannot delete admin account.");
            throw new ValidationException("Cannot delete admin account.");
        }

        accountService.deleteAccount(deleteUserId, null);
    }
}
