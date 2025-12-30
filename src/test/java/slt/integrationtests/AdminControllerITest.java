package slt.integrationtests;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.http.HttpStatus;
import slt.database.entities.UserAccount;
import slt.security.ThreadLocalHolder;
import slt.security.UserInfo;
import slt.integrationtests.utils.AbstractApplicationIntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AdminControllerITest extends AbstractApplicationIntegrationTest {

    private Long userId;

    @BeforeEach
    public void setUserContext() {
        if (this.userId == null) {
            log.debug("Creating test user for test " + this.getClass().getName());
            this.userId = createUser(this.getClass().getName());
        }
        final var userInfo = new UserInfo();
        userInfo.setUserId(this.userId);
        ThreadLocalHolder.getThreadLocal().set(userInfo);
    }

    @Test
    public void testGetUsers() {
        final var allUsers = adminController.getAllUsers();
        assertThat(allUsers.getBody()).isNull();
        assertThat(allUsers.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void deleteAccount() {
        var adminUser = UserAccount.builder().isAdmin(true).email("admin@test.nl").userName("iamadmin").password("rtyufaai").build();
        adminUser = userAccountRepository.saveAccount(adminUser);
        this.userId = adminUser.getId();
        ThreadLocalHolder.getThreadLocal().set(UserInfo.builder().userId(this.userId).build());

        var toBeDeletedUser = UserAccount.builder().isAdmin(false).email("test@test.nl").userName("someone").password("fghjkbvc").build();
        toBeDeletedUser = userAccountRepository.saveAccount(toBeDeletedUser);

        final var allUsers = adminController.getAllUsers();
        final var usersAccounts = allUsers.getBody();
        Assertions.assertNotNull(usersAccounts);
        Assertions.assertTrue(usersAccounts.stream().anyMatch(u -> u.getUserName().equals("someone")));

        final var response = adminController.deleteAccount(toBeDeletedUser.getId());
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());

        final var allUsersAfterDelete = adminController.getAllUsers();
        final var usersAccountsAfterDelete = allUsersAfterDelete.getBody();
        Assertions.assertNotNull(usersAccountsAfterDelete);
        Assertions.assertFalse(usersAccountsAfterDelete.stream().anyMatch(u -> u.getUserName().equals("someone")));

    }
}
