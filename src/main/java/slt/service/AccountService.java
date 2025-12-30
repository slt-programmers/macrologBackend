package slt.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.stereotype.Service;
import slt.database.*;
import slt.database.entities.UserAccount;
import slt.dto.UserAccountDto;
import slt.exceptions.NotFoundException;
import slt.exceptions.UnauthorizedException;
import slt.exceptions.ValidationException;
import slt.mapper.UserAccountMapper;
import slt.security.ThreadLocalHolder;
import slt.util.JWTBuilder;
import slt.util.PasswordUtils;

import java.time.LocalDateTime;

@Slf4j
@Service
@AllArgsConstructor
public class AccountService {

    private GoogleMailService mailService;

    private ActivityRepository activityRepository;
    private EntryRepository entryRepository;
    private DishRepository dishRepository;
    private MealplanRepository mealplanRepository;
    private SettingsRepository settingsRepository;
    private UserAccountRepository userAccountRepository;
    private WeightRepository weightRepository;
    private FoodRepository foodRepository;

    private final UserAccountMapper userAccountMapper = UserAccountMapper.INSTANCE;

    public UserAccountDto getAccount(final String username, final String password) {
        final var userAccount = getAccountByNameOrEmail(username);
        validatePassword(password, userAccount);
        final var jwt = new JWTBuilder().generateJWT(userAccount.getUserName(), userAccount.getId());
        return userAccountMapper.map(userAccount, jwt);
    }

    public UserAccountDto registerAccount(final String username, final String email, final String password) {
        verifyUsernameAndEmailUnused(username, email);
        final var newAccount = userAccountRepository.insertUser(username, password, email);
        final var jwt = new JWTBuilder().generateJWT(username, newAccount.getId());
        mailService.sendConfirmationMail(email, newAccount);
        return userAccountMapper.map(newAccount, jwt);
    }

    public void deleteAccount(final Long userId, final String password) {
        final var optionalAccount = userAccountRepository.getUserById(userId);
        if (optionalAccount.isEmpty()) {
            log.error("Account not found for userId [{}]", userId);
            throw new NotFoundException("Useraccount not found.");
        }
        final var userAccount = optionalAccount.get();
        if (!isAdmin() && !userAccount.getPassword().equals(password)) {
            log.error("Could not delete account: password incorrect");
            throw new UnauthorizedException("Could not delete account.");
        }

        activityRepository.deleteAllForUser(userId);
        entryRepository.deleteAllForUser(userId);
        dishRepository.deleteAllForUser(userId);
        mealplanRepository.deleteAllForUser(userId);
        foodRepository.deleteAllForUser(userId);
        weightRepository.deleteAllForUser(userId);
        settingsRepository.deleteAllForUser(userId);
        userAccountRepository.deleteUser(userId);
    }

    public void changePassword(final Long userId, final String oldPassword, final String newPassword, final String confirmPassword) {
        final var userAccount = getAccountByUserId(userId);

        if (!userAccount.getPassword().equals(oldPassword)) {
            throw new UnauthorizedException("Old password incorrect.");
        }

        if (!newPassword.equals(confirmPassword)) {
            throw new ValidationException("Passwords do not match.");
        }

        userAccount.setPassword(newPassword);
        userAccount.setResetDate(null);
        userAccount.setResetPassword(null);
        userAccountRepository.saveAccount(userAccount);
    }

    public void sendPasswordRetrievalMail(final String email) {
        final var optionalUserAccount = userAccountRepository.getUserByEmail(email);
        if (optionalUserAccount.isPresent()) {
            final var account = optionalUserAccount.get();
            final var randomPassword = RandomStringUtils.randomAlphabetic(10);
            final var hashedRandomPassword = PasswordUtils.hashPassword(randomPassword);
            account.setResetPassword(hashedRandomPassword);
            account.setResetDate(LocalDateTime.now());
            userAccountRepository.saveAccount(account);
            mailService.sendPasswordRetrievalMail(email, randomPassword, account);
        } else {
            throw new NotFoundException("Useraccount not found.");
        }
    }

    public boolean isAdmin() {
        final var userInfo = ThreadLocalHolder.getThreadLocal().get();
        final var optionalAdminUser =  userAccountRepository.getUserById(userInfo.getUserId());
        return optionalAdminUser.isPresent() && optionalAdminUser.get().isAdmin();
    }

    private UserAccount getAccountByUserId(final Long userId) {
        final var optionalUserAccount = userAccountRepository.getUserById(userId);
        if (optionalUserAccount.isEmpty()) {
            throw new NotFoundException("Useraccount not found.");
        }
        return optionalUserAccount.get();
    }

    private UserAccount getAccountByNameOrEmail(final String usernameOrEmail) {
        final var optionalAccountByName = userAccountRepository.getUserByName(usernameOrEmail);
        if (optionalAccountByName.isPresent()) return optionalAccountByName.get();

        final var optionalAccountByEmail = userAccountRepository.getUserByEmail(usernameOrEmail);
        if (optionalAccountByEmail.isPresent()) return optionalAccountByEmail.get();

        throw new NotFoundException("Useraccount not found.");
    }

    private void verifyUsernameAndEmailUnused(final String username, final String email) {
        final var optionalAccount = userAccountRepository.getUserByName(username);
        if (optionalAccount.isPresent()) {
            throw new UnauthorizedException("Username or email already in use.");
        } else {
            final var optionalAccountEmail = userAccountRepository.getUserByEmail(email);
            if (optionalAccountEmail.isPresent()) {
                throw new UnauthorizedException("Username or email already in use.");
            }
        }
    }


    private void validatePassword(final String hashedPassword, final UserAccount account) {
        boolean activePasswordOK = account.getPassword().equals(hashedPassword);
        if (!activePasswordOK) {
            boolean resettedPasswordOK = account.getResetPassword() != null &&
                    account.getResetPassword().equals(hashedPassword);

            boolean withinTimeFrame = account.getResetDate() != null &&
                    account.getResetDate().isAfter(LocalDateTime.now().minusMinutes(30));

            if (resettedPasswordOK && withinTimeFrame) {
                log.info("Password has been reset to verified new password");
                account.setPassword(hashedPassword);
                account.setResetDate(null);
                account.setResetDate(null);
                userAccountRepository.saveAccount(account);
            } else {
                throw new UnauthorizedException("Username or password incorrect.");
            }
        }
    }

}
