package csl.rest;

import csl.database.SettingsRepository;
import csl.database.model.Setting;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

@RestController
@RequestMapping("/settings")
@Api(value = "settings", description = "Operations pertaining personal settings")
public class SettingsService {

    private SettingsRepository settingsRepo = new SettingsRepository();

    @ApiOperation(value = "Store new setting or change existing one")
    @CrossOrigin(origins = "http://localhost:4200")
    @RequestMapping(value = "",
            method = PUT,
            headers = {"Content-Type=application/json"})
    public ResponseEntity changeSetting(@RequestBody Setting setting) {
        settingsRepo.putSetting(setting.getName(), setting.getValue());
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @ApiOperation(value = "Get all settings")
    @CrossOrigin(origins = "http://localhost:4200")
    @RequestMapping(value = "",
            method = GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getAllSetting() {
        List<Setting> settings = settingsRepo.getAllSettings();
        return ResponseEntity.ok(settings);
    }

    @ApiOperation(value = "Get setting")
    @CrossOrigin(origins = "http://localhost:4200")
    @RequestMapping(value = "/{name}",
            method = GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getSetting(@PathVariable("name") String name) {
        String setting = settingsRepo.getSetting(name);
        return ResponseEntity.ok(setting);
    }

}
