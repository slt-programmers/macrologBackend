package slt.rest;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import slt.dto.ConnectivityRequestDto;
import slt.dto.ConnectivityStatusDto;
import slt.dto.MailDto;
import slt.dto.UserAccountDto;
import slt.service.AdminService;
import slt.service.GoogleMailService;

import java.util.List;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    private AdminService adminService;
    private GoogleMailService mailService;

    @GetMapping(path = "/getAllUsers")
    public ResponseEntity<List<UserAccountDto>> getAllUsers() {
        adminService.verifyAdmin();
        final var allUserAccountDtos = adminService.getAllUsers();
        return ResponseEntity.ok(allUserAccountDtos);
    }

    @PostMapping(path = "/deleteAccount")
    public ResponseEntity<Void> deleteAccount(@RequestParam("userId") final Long deleteUserId) {
        adminService.verifyAdmin();
        adminService.deleteUserAccount(deleteUserId);
        return ResponseEntity.ok().build();
    }

    @GetMapping(path = "/mail/status")
    public ResponseEntity<ConnectivityStatusDto> getMailStatus() {
        adminService.verifyAdmin();
        return ResponseEntity.ok(mailService.getMailStatus());
    }

    @PostMapping(path = "/mail")
    public ResponseEntity<Void> postMailSetting(@RequestBody final ConnectivityRequestDto connectivityRequestDto) {
        adminService.verifyAdmin();
        log.debug("Handling connectivity request");
        mailService.registerWithCode(connectivityRequestDto.getClientAuthorizationCode());
        return ResponseEntity.ok().build();
    }

    @PostMapping(path = "/mail/testmail")
    public ResponseEntity<Void> sendTestMail(@RequestBody final MailDto mailRequest) {
        adminService.verifyAdmin();
        log.debug("Sending mail to email [{}]", mailRequest.getEmailTo());
        mailService.sendTestMail(mailRequest.getEmailTo());
        return ResponseEntity.ok().build();
    }

}
