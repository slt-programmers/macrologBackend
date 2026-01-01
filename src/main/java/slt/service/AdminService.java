package slt.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import slt.database.UserAccountRepository;
import slt.dto.UserAccountDto;
import slt.exceptions.UnauthorizedException;
import slt.mapper.UserAccountMapper;
import slt.security.ThreadLocalHolder;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class AdminService {

    private AccountService accountService;
    private UserAccountRepository userAccountRepository;

    private final UserAccountMapper userAccountMapper = UserAccountMapper.INSTANCE;

    public void verifyAdmin() {
        verifyAdmin("Not authorized as admin");
    }

    public void verifyAdmin(final String message) {
        final var userInfo = ThreadLocalHolder.getThreadLocal().get();
        final var optionalAdminUser =  userAccountRepository.getUserById(userInfo.getUserId());
        if (!(optionalAdminUser.isPresent() && optionalAdminUser.get().isAdmin())) {
            throw new UnauthorizedException(message);
        }
    }

    public List<UserAccountDto> getAllUsers() {
        final var userAccounts = userAccountRepository.getAllUsers();
        return userAccountMapper.map(userAccounts);
    }

    public void deleteUserAccount(final Long deleteUserId) {
        accountService.deleteAccountAsAdmin(deleteUserId);
    }
}
