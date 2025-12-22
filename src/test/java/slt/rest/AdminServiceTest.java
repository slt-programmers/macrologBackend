package slt.rest;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;
import slt.database.UserAccountRepository;
import slt.database.entities.UserAccount;
import slt.dto.ConnectivityRequestDto;
import slt.dto.ConnectivityStatusDto;
import slt.dto.MailDto;
import slt.dto.UserAccountDto;
import slt.security.ThreadLocalHolder;
import slt.security.UserInfo;
import slt.service.GoogleMailService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@RunWith(MockitoJUnitRunner.class)
class AdminServiceTest {

    @Mock
    UserAccountRepository userRepo;

    @Mock
    AccountService accountService;

    @Mock
    GoogleMailService googleMailService;

    @InjectMocks
    AdminService adminService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(123L);
        ThreadLocalHolder.getThreadLocal().set(userInfo);
        reset(accountService,googleMailService,userRepo);
    }

    @Test
    void getAllUsers() {
        UserAccount adminUser = new UserAccount();
        adminUser.setId(123L);
        adminUser.setAdmin(true);
        when(userRepo.getUserById(123L)).thenReturn(adminUser);
        UserAccount someUser = new UserAccount();
        someUser.setId(234L);
        List<UserAccount> users = new ArrayList<>();
        users.add(someUser);
        users.add(adminUser);
        when(userRepo.getAllUsers()).thenReturn(users);

        final var response = adminService.getAllUsers();
        Assertions.assertNotNull(response.getBody());
        List<UserAccountDto> userDtos = response.getBody();

        Assertions.assertEquals(2, userDtos.size());
        Assertions.assertEquals(234L, userDtos.get(0).getId().intValue());
        Assertions.assertEquals(123L, userDtos.get(1).getId().intValue());
        verify(userRepo).getAllUsers();
    }

    @Test
    void getAllUsersUnauthorized() {
        UserAccount nonAdminUser = new UserAccount();
        nonAdminUser.setId(123L);
        when(userRepo.getUserById(123L)).thenReturn(nonAdminUser);
        UserAccount someUser = new UserAccount();
        someUser.setId(234L);
        List<UserAccount> users = new ArrayList<>();
        users.add(someUser);
        users.add(nonAdminUser);
        when(userRepo.getAllUsers()).thenReturn(users);

        ResponseEntity response = adminService.getAllUsers();
        Assertions.assertEquals(401, response.getStatusCodeValue());
        verify(userRepo, times(0)).getAllUsers();
    }

    @Test
    void deleteAccount() {
        UserAccount adminUser = new UserAccount();
        adminUser.setId(123L);
        adminUser.setAdmin(true);

        UserAccount toBeDeletedUser = new UserAccount();
        toBeDeletedUser.setId(234L);
        when(userRepo.getUserById(123L)).thenReturn(adminUser);
        when(userRepo.getUserById(234L)).thenReturn(toBeDeletedUser);

        ResponseEntity response = adminService.deleteAccount(234L);
        Assertions.assertEquals(200, response.getStatusCodeValue());
        verify(accountService).deleteAccount(234L);
    }

    @Test
    void deleteAccountNotFound() {
        UserAccount adminUser = new UserAccount();
        adminUser.setId(123L);
        adminUser.setAdmin(true);

        when(userRepo.getUserById(123L)).thenReturn(adminUser);
        when(userRepo.getUserById(234L)).thenReturn(null);

        ResponseEntity response = adminService.deleteAccount(234L);
        Assertions.assertEquals(404, response.getStatusCodeValue());
        Mockito.verifyNoInteractions(accountService);
    }


    @Test
    void deleteAccountAdminItself() {
        UserAccount adminUser = new UserAccount();
        adminUser.setId(123L);
        adminUser.setAdmin(true);

        when(userRepo.getUserById(123L)).thenReturn(adminUser);
        when(userRepo.getUserById(123L)).thenReturn(adminUser);

        ResponseEntity response = adminService.deleteAccount(123L);
        Assertions.assertEquals(400, response.getStatusCodeValue());
        Mockito.verifyNoInteractions(accountService);
    }


    @Test
    void deleteAccountUnauthorized() {
        UserAccount adminUser = new UserAccount();
        adminUser.setId(123L);

        UserAccount toBeDeletedUser = new UserAccount();
        adminUser.setId(234L);
        when(userRepo.getUserById(123L)).thenReturn(adminUser);
        when(userRepo.getUserById(234L)).thenReturn(toBeDeletedUser);

        ResponseEntity response = adminService.deleteAccount(234L);
        Assertions.assertEquals(401, response.getStatusCodeValue());
        Mockito.verifyNoInteractions(accountService);
    }

    @Test
    void getMailConfigUnauthorized() {
        UserAccount nonAdminUser = new UserAccount();
        nonAdminUser.setId(123L);

        when(userRepo.getUserById(123L)).thenReturn(nonAdminUser);

        ResponseEntity response = adminService.getMailStatus();
        Assertions.assertEquals(401, response.getStatusCodeValue());
        Mockito.verifyNoInteractions(accountService);
        Mockito.verifyNoInteractions(googleMailService);
    }
    @Test
    void storeMailSettingUnauthorized() throws IOException {
        UserAccount nonAdminUser = new UserAccount();
        nonAdminUser.setId(123L);

        when(userRepo.getUserById(123L)).thenReturn(nonAdminUser);

        ResponseEntity response = adminService.storeMailSetting(ConnectivityRequestDto.builder().clientAuthorizationCode("a").build());
        Assertions.assertEquals(401, response.getStatusCodeValue());
        Mockito.verifyNoInteractions(accountService);
        Mockito.verifyNoInteractions(googleMailService);
    }
    @Test
    void sendTestMailUnauthorized() {
        UserAccount nonAdminUser = new UserAccount();
        nonAdminUser.setId(123L);

        when(userRepo.getUserById(123L)).thenReturn(nonAdminUser);

        ResponseEntity response = adminService.sendTestMail(MailDto.builder().build());
        Assertions.assertEquals(401, response.getStatusCodeValue());
        Mockito.verifyNoInteractions(accountService);
        Mockito.verifyNoInteractions(googleMailService);
    }

    @Test
    void getMailConfigAuthorized(){
        UserAccount adminUser = new UserAccount();
        adminUser.setId(123L);
        adminUser.setAdmin(true);

        when(userRepo.getUserById(123L)).thenReturn(adminUser);
        when(googleMailService.getMailStatus()).thenReturn(ConnectivityStatusDto.builder().build());

        ResponseEntity response = adminService.getMailStatus();
        Assertions.assertEquals(200, response.getStatusCodeValue());
        Mockito.verifyNoInteractions(accountService);
        verify(googleMailService).getMailStatus();

    }

    @Test
    void storeMailSettingAuthorized() throws IOException {
        UserAccount adminUser = new UserAccount();
        adminUser.setId(123L);
        adminUser.setAdmin(true);

        when(userRepo.getUserById(123L)).thenReturn(adminUser);

        ResponseEntity response = adminService.storeMailSetting(ConnectivityRequestDto.builder().clientAuthorizationCode("a").build());
        Assertions.assertEquals(200, response.getStatusCodeValue());
        Mockito.verifyNoInteractions(accountService);
        verify(googleMailService).registerWithCode(eq("a"));
    }
    @Test
    void sendTestMailSettingAuthorized() {
        UserAccount adminUser = new UserAccount();
        adminUser.setId(123L);
        adminUser.setAdmin(true);

        when(userRepo.getUserById(123L)).thenReturn(adminUser);

        ResponseEntity response = adminService.sendTestMail(MailDto.builder().emailTo("a").build());
        Assertions.assertEquals(200, response.getStatusCodeValue());
        Mockito.verifyNoInteractions(accountService);
        verify(googleMailService).sendTestMail(eq("a"));
    }

}