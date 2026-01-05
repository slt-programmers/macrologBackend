package slt.rest;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import slt.config.StravaConfig;
import slt.connectivity.strava.StravaActivityService;
import slt.connectivity.strava.dto.SubscriptionInformation;
import slt.connectivity.strava.dto.WebhookEvent;
import slt.service.AdminService;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/webhooks")
public class WebhookController {

    public static final String NOT_AUTHORIZED_TO_ALTER_WEBHOOKS_MESSAGE = "Not authorized to alter webhooks";

    private StravaActivityService stravaActivityService;
    private AdminService adminService;
    private StravaConfig stravaConfig;

    @PostMapping(path = "public/strava")
    public ResponseEntity<Void> syncStrava(@RequestBody final WebhookEvent event) {
        stravaActivityService.receiveWebhookEvent(event);
        return ResponseEntity.ok().build();
    }

    @GetMapping(path = "public/strava")
    public ResponseEntity<String> syncStravaCallback(@RequestParam(name = "hub.mode") final String hubMode,
                                                     @RequestParam(name = "hub.challenge") final String hubChallenge,
                                                     @RequestParam(name = "hub.verify_token") final String hubVerifyToken) {
        log.debug("Received strava callback {} {} {}", hubMode, hubChallenge, hubVerifyToken);
        if (!"subscribe".equals(hubMode) || !stravaConfig.getVerifytoken().equals(hubVerifyToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } else {
            final var ret = "{\"hub.challenge\":\"" + hubChallenge + "\"}";
            return ResponseEntity.ok(ret);
        }
    }

    @PostMapping(path = "/STRAVA")
    public ResponseEntity<SubscriptionInformation> startWebhook() {
        adminService.verifyAdmin(NOT_AUTHORIZED_TO_ALTER_WEBHOOKS_MESSAGE);
        final var subscriptionInformation = stravaActivityService.startWebhookSubcription();
        return ResponseEntity.ok(subscriptionInformation);
    }

    @DeleteMapping(path = "/STRAVA/{subscriptionId}")
    public ResponseEntity<Void> endWebhook(@PathVariable("subscriptionId") final Integer subscriptionId) {
        adminService.verifyAdmin(NOT_AUTHORIZED_TO_ALTER_WEBHOOKS_MESSAGE);
        stravaActivityService.endWebhookSubscription(subscriptionId);
        return ResponseEntity.ok().build();
    }

    @GetMapping(path = "/STRAVA")
    public ResponseEntity<SubscriptionInformation> getWebhook() {
        adminService.verifyAdmin(NOT_AUTHORIZED_TO_ALTER_WEBHOOKS_MESSAGE);
        final var subscriptionInformation = stravaActivityService.getWebhookSubscription();
        return ResponseEntity.ok(subscriptionInformation);
    }
}

