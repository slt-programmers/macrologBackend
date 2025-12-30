package slt.rest;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import slt.dto.*;
import slt.security.ThreadLocalHolder;
import slt.service.SettingsService;
import slt.util.DateUtils;

@AllArgsConstructor
@RestController
@RequestMapping("/settings")
public class SettingsController {

    private SettingsService settingsService;

    @GetMapping(path = "/user")
    public ResponseEntity<UserSettingsDto> getUserSettings() {
        final var userInfo = ThreadLocalHolder.getThreadLocal().get();
        final var userSettingsDto = settingsService.getUserSettingsDto(userInfo.getUserId());
        return ResponseEntity.ok(userSettingsDto);
    }

    @GetMapping(path = "/{name}")
    public ResponseEntity<String> getSetting(@PathVariable("name") final String name,
                                             @RequestParam(value = "date", required = false) final String toDate) {
        final var userInfo = ThreadLocalHolder.getThreadLocal().get();
        DateUtils.validateDateFormat(toDate);
        final var settingValue = settingsService.getSetting(userInfo.getUserId(), name, toDate);
        return ResponseEntity.ok(settingValue);
    }

    @PutMapping
    public ResponseEntity<Void> putSetting(@RequestBody final SettingDto settingDto) {
        final var userInfo = ThreadLocalHolder.getThreadLocal().get();
        settingsService.putSetting(userInfo.getUserId(), settingDto);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

}
