package slt.rest;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import slt.dto.UserAccountDto;
import slt.security.ThreadLocalHolder;
import slt.security.UserInfo;
import slt.service.AdminService;
import slt.service.GoogleMailService;

import java.util.List;

import static org.mockito.Mockito.*;

@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@RunWith(MockitoJUnitRunner.class)
class AdminControllerTest {

    private AdminController controller;
    private AdminService adminService;

    @BeforeEach
    void setUp() {
        adminService = mock(AdminService.class);
        GoogleMailService mailService = mock(GoogleMailService.class);
        controller = new AdminController(adminService, mailService);
        final var userInfo = new UserInfo();
        userInfo.setUserId(123L);
        ThreadLocalHolder.getThreadLocal().set(userInfo);
    }

    @Test
    void getAllUsers() {
        final var adminUser = UserAccountDto.builder()
                .id(123L)
                .userName("iamAdmin")
                .isAdmin(true)
                .build();
        final var someUser =  UserAccountDto.builder().userName("anotherUser").id(234L).build();
        when(adminService.getAllUsers()).thenReturn(List.of(adminUser, someUser));

        final var response = controller.getAllUsers();
        Assertions.assertNotNull(response.getBody());
        final var userDtos = response.getBody();

        Assertions.assertEquals(2, userDtos.size());
        Assertions.assertEquals(123L, userDtos.get(0).getId().intValue());
        Assertions.assertEquals(234L, userDtos.get(1).getId().intValue());
        verify(adminService).getAllUsers();
        verify(adminService).verifyAdmin();
    }

//    @Test
//    void deleteAccount() {
//        final var adminUser = new UserAccount();
//        adminUser.setId(123L);
//        adminUser.setAdmin(true);
//
//        final var toBeDeletedUser = new UserAccount();
//        toBeDeletedUser.setId(234L);
//        when(userRepo.getUserById(123L)).thenReturn(Optional.of(adminUser));
//        when(userRepo.getUserById(234L)).thenReturn(Optional.of(toBeDeletedUser));
//
//        final var response = adminController.deleteAccount(234L);
//        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
//        verify(accountService).deleteAccount(234L, null);
//    }
//
//    @Test
//    void deleteAccountNotFound() {
//        UserAccount adminUser = new UserAccount();
//        adminUser.setId(123L);
//        adminUser.setAdmin(true);
//
//        when(userRepo.getUserById(123L)).thenReturn(Optional.of(adminUser));
//        when(userRepo.getUserById(234L)).thenReturn(Optional.empty());
//
//        final var response = adminController.deleteAccount(234L);
//        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
//        Mockito.verifyNoInteractions(accountService);
//    }
//
//
//    @Test
//    void deleteAccountAdminItself() {
//        UserAccount adminUser = new UserAccount();
//        adminUser.setId(123L);
//        adminUser.setAdmin(true);
//
//        when(userRepo.getUserById(123L)).thenReturn(Optional.of(adminUser));
//        when(userRepo.getUserById(123L)).thenReturn(Optional.of(adminUser));
//
//        final var response = adminController.deleteAccount(123L);
//        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
//        Mockito.verifyNoInteractions(accountService);
//    }
//
//
//    @Test
//    void deleteAccountUnauthorized() {
//        UserAccount adminUser = new UserAccount();
//        adminUser.setId(123L);
//
//        UserAccount toBeDeletedUser = new UserAccount();
//        adminUser.setId(234L);
//        when(userRepo.getUserById(123L)).thenReturn(Optional.of(adminUser));
//        when(userRepo.getUserById(234L)).thenReturn(Optional.of(toBeDeletedUser));
//
//        final var response = adminController.deleteAccount(234L);
//        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
//        Mockito.verifyNoInteractions(accountService);
//    }
//
//    @Test
//    void getMailConfigUnauthorized() {
//        UserAccount nonAdminUser = new UserAccount();
//        nonAdminUser.setId(123L);
//
//        when(userRepo.getUserById(123L)).thenReturn(Optional.of(nonAdminUser));
//
//        final var response = adminController.getMailStatus();
//        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
//        Mockito.verifyNoInteractions(accountService);
//        Mockito.verifyNoInteractions(googleMailService);
//    }
//
//    @Test
//    void postMailSettingUnauthorized() {
//        UserAccount nonAdminUser = new UserAccount();
//        nonAdminUser.setId(123L);
//
//        when(userRepo.getUserById(123L)).thenReturn(Optional.of(nonAdminUser));
//
//        final var response = adminController.postMailSetting(ConnectivityRequestDto.builder().clientAuthorizationCode("a").build());
//        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
//        Mockito.verifyNoInteractions(accountService);
//        Mockito.verifyNoInteractions(googleMailService);
//    }
//
//    @Test
//    void sendTestMailUnauthorized() {
//        UserAccount nonAdminUser = new UserAccount();
//        nonAdminUser.setId(123L);
//
//        when(userRepo.getUserById(123L)).thenReturn(Optional.of(nonAdminUser));
//
//        final var response = adminController.sendTestMail(MailDto.builder().build());
//        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
//        Mockito.verifyNoInteractions(accountService);
//        Mockito.verifyNoInteractions(googleMailService);
//    }
//
//    @Test
//    void getMailConfigAuthorized() {
//        UserAccount adminUser = new UserAccount();
//        adminUser.setId(123L);
//        adminUser.setAdmin(true);
//
//        when(userRepo.getUserById(123L)).thenReturn(Optional.of(adminUser));
//        when(googleMailService.getMailStatus()).thenReturn(ConnectivityStatusDto.builder().build());
//
//        final var response = adminController.getMailStatus();
//        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
//        Mockito.verifyNoInteractions(accountService);
//        verify(googleMailService).getMailStatus();
//
//    }
//
//    @Test
//    void postMailSettingAuthorized() {
//        UserAccount adminUser = new UserAccount();
//        adminUser.setId(123L);
//        adminUser.setAdmin(true);
//
//        when(userRepo.getUserById(123L)).thenReturn(Optional.of(adminUser));
//
//        final var response = adminController.postMailSetting(ConnectivityRequestDto.builder().clientAuthorizationCode("a").build());
//        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
//        Mockito.verifyNoInteractions(accountService);
//        verify(googleMailService).registerWithCode(eq("a"));
//    }
//
//    @Test
//    void sendTestMailSettingAuthorized() {
//        UserAccount adminUser = new UserAccount();
//        adminUser.setId(123L);
//        adminUser.setAdmin(true);
//
//        when(userRepo.getUserById(123L)).thenReturn(Optional.of(adminUser));
//
//        final var response = adminController.sendTestMail(MailDto.builder().emailTo("a").build());
//        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
//        Mockito.verifyNoInteractions(accountService);
//        verify(googleMailService).sendTestMail(eq("a"));
//    }

}