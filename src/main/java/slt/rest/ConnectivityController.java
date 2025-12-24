package slt.rest;

import lombok.AllArgsConstructor;
import org.apache.commons.lang.NotImplementedException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import slt.connectivity.strava.StravaActivityService;
import slt.dto.SettingDto;
import slt.dto.SyncedAccount;
import slt.security.ThreadLocalHolder;

@RestController
@RequestMapping("/settings/connectivity")

@AllArgsConstructor
public class ConnectivityController {

    private StravaActivityService stravaActivityService;

    @GetMapping(path = "/{name}")
    public ResponseEntity<SyncedAccount> getConnectivitySetting(@PathVariable("name") final String name) {
        final var userInfo = ThreadLocalHolder.getThreadLocal().get();
        if (name.equals("STRAVA")) {
            final var syncedAccount = stravaActivityService.getStravaConnectivity(userInfo.getUserId());
            return ResponseEntity.ok(syncedAccount);
        }
        throw new NotImplementedException();
    }

    @PostMapping(path = "/{name}")
    public ResponseEntity<SyncedAccount> postConnectivitySetting(@RequestBody final SettingDto code) {
        final var userInfo = ThreadLocalHolder.getThreadLocal().get();
        if (code.getName().equals("STRAVA")) {
            final var syncedAccount = stravaActivityService.registerStravaConnectivity(userInfo.getUserId(), code.getValue());
            return ResponseEntity.ok(syncedAccount);
        }
        throw new NotImplementedException();
    }

    @DeleteMapping(path = "/{name}")
    public ResponseEntity<Void> deleteConnectivitySetting(@PathVariable("name") final String name) {
        final var userInfo = ThreadLocalHolder.getThreadLocal().get();
        if (name.equals("STRAVA")) {
            stravaActivityService.unregisterStrava(userInfo.getUserId());
            return ResponseEntity.ok().build();
        }
        throw new NotImplementedException();
    }

}
