package slt.rest;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;
import slt.database.UserAccountRepository;
import slt.database.entities.UserAccount;
import slt.security.ThreadLocalHolder;
import slt.security.UserInfo;

@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@RunWith(MockitoJUnitRunner.class)
class AdminServiceTest {

    @Mock
    UserAccountRepository userRepo;

    @Mock
    AccountService accountService;

    @InjectMocks
    AdminService adminService;

    @BeforeAll
    void setUp() {
        MockitoAnnotations.initMocks(this);
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(123);
        ThreadLocalHolder.getThreadLocal().set(userInfo);
    }

    @Test
    void deleteAccount() {
        UserAccount adminUser = new UserAccount();
        adminUser.setId(123);
        adminUser.setAdmin(true);

        UserAccount toBeDeletedUser = new UserAccount();
        toBeDeletedUser.setId(234);
        Mockito.when(userRepo.getUserById(123)).thenReturn(adminUser);
        Mockito.when(userRepo.getUserById(234)).thenReturn(toBeDeletedUser);

        ResponseEntity response = adminService.deleteAccount(234);
        Assertions.assertEquals(200, response.getStatusCodeValue());
        Mockito.verify(accountService).deleteAccount(234);
    }

    @Test
    void deleteAccountNotFound() {
        UserAccount adminUser = new UserAccount();
        adminUser.setId(123);
        adminUser.setAdmin(true);

        Mockito.when(userRepo.getUserById(123)).thenReturn(adminUser);
        Mockito.when(userRepo.getUserById(234)).thenReturn(null);

        ResponseEntity response = adminService.deleteAccount(234);
        Assertions.assertEquals(404, response.getStatusCodeValue());
        Mockito.verifyZeroInteractions(accountService);
    }

    @Test
    void deleteAccountUnauthorized() {
        UserAccount adminUser = new UserAccount();
        adminUser.setId(123);

        UserAccount toBeDeletedUser = new UserAccount();
        adminUser.setId(234);
        Mockito.when(userRepo.getUserById(123)).thenReturn(adminUser);
        Mockito.when(userRepo.getUserById(234)).thenReturn(toBeDeletedUser);

        ResponseEntity response = adminService.deleteAccount(234);
        Assertions.assertEquals(401, response.getStatusCodeValue());
        Mockito.verifyZeroInteractions(accountService);
    }
}