package slt.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import slt.database.UserAccountRepository;
import slt.database.entities.UserAccount;
import slt.exceptions.UnauthorizedException;
import slt.security.ThreadLocalHolder;
import slt.security.UserInfo;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

public class AdminServiceTest {

    private AdminService service;
    private AccountService accountService;
    private UserAccountRepository repository;

    @BeforeEach
    void setup() {
        repository = mock(UserAccountRepository.class);
        accountService = mock(AccountService.class);
        service = new AdminService(accountService, repository);
    }

    @Test
    void testGetAllUsers() {
        final var someUser = UserAccount.builder()
                .id(1L)
                .isAdmin(false)
                .userName("name")
                .email("test@test.nl")
                .build();
        when(repository.getAllUsers()).thenReturn(List.of(someUser));
        final var result = service.getAllUsers();

        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());
        final var accountDto = result.getFirst();
        Assertions.assertEquals(1L, accountDto.getId());
        Assertions.assertEquals("name", accountDto.getUserName());
        Assertions.assertEquals("test@test.nl", accountDto.getEmail());
        Assertions.assertFalse(accountDto.isAdmin());
        Assertions.assertNull(accountDto.getToken());
    }

    @Test
    void testVerifyAdmin() {
        final var nonAdmin = UserAccount.builder().id(123L).build();
        ThreadLocalHolder.getThreadLocal().set(UserInfo.builder().userId(123L).build());
        when(repository.getUserById(123L)).thenReturn(Optional.ofNullable(nonAdmin));
        Assertions.assertThrows(UnauthorizedException.class, () -> service.verifyAdmin("Nope"));

        final var admin = UserAccount.builder().id(234L).isAdmin(true).build();
        ThreadLocalHolder.getThreadLocal().set(UserInfo.builder().userId(234L).build());
        when(repository.getUserById(234L)).thenReturn(Optional.ofNullable(admin));

        service.verifyAdmin();
        verify(repository).getUserById(234L);
    }

    @Test
    void deleteAccount() {
        service.deleteUserAccount(123L);
        verify(accountService).deleteAccountAsAdmin(123L);
    }
}
