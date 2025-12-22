package slt.rest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import slt.config.StravaConfig;
import slt.connectivity.strava.StravaActivityService;
import slt.connectivity.strava.dto.SubscriptionInformation;
import slt.connectivity.strava.dto.WebhookEvent;
import slt.database.UserAccountRepository;
import slt.security.ThreadLocalHolder;

@Slf4j
@RestController
@RequestMapping("/webhooks")
public class WebHookService {

    public static final String NOT_AUTHORIZED_TO_ALTER_WEBHOOKS_MESSAGE = "Not authorized to alter webhooks";
    @Autowired
    private StravaActivityService stravaActivityService;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private StravaConfig stravaConfig;

    @PostMapping(path = "public/strava", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> syncStrava(@RequestBody WebhookEvent event) {
        stravaActivityService.receiveWebHookEvent(event);
        return ResponseEntity.ok().build();
    }

    @GetMapping(path = "public/strava", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> syncStravaCallback(@RequestParam(name = "hub.mode") String hubMode,
                                                   @RequestParam(name = "hub.challenge") String hubChallenge,
                                                   @RequestParam(name = "hub.verify_token") String hubVerifyToken) {
        log.debug("Received strava callback {} {} {}", hubMode, hubChallenge, hubVerifyToken);
        if (!"subscribe".equals(hubMode) || !stravaConfig.getVerifytoken().equals(hubVerifyToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } else {
            String ret = "{\"hub.challenge\":\"" + hubChallenge + "\"}";
            return ResponseEntity.ok(ret);
        }
    }

    @PostMapping(path = "/STRAVA", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SubscriptionInformation> startWebhook() {
        final var userInfo = ThreadLocalHolder.getThreadLocal().get();
        final var userId = userInfo.getUserId();
        final var userAccount = userAccountRepository.getUserById(userId);
        if (!userAccount.isAdmin()) {
            log.error(NOT_AUTHORIZED_TO_ALTER_WEBHOOKS_MESSAGE);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } else {
            final SubscriptionInformation subscriptionInformation = stravaActivityService.startWebhookSubcription();
            return ResponseEntity.ok(subscriptionInformation);
        }
    }

    @DeleteMapping(path = "/STRAVA/{subscriptionId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> endWebhook(@PathVariable("subscriptionId") Integer subscriptionId) {
        final var userInfo = ThreadLocalHolder.getThreadLocal().get();
        final var userId = userInfo.getUserId();
        final var userAccount = userAccountRepository.getUserById(userId);
        if (!userAccount.isAdmin()) {
            log.error(NOT_AUTHORIZED_TO_ALTER_WEBHOOKS_MESSAGE);
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } else {
            stravaActivityService.endWebhookSubscription(subscriptionId);
            return ResponseEntity.ok().build();
        }
    }

    @GetMapping(path = "/STRAVA", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SubscriptionInformation> getWebhook() {
        final var userInfo = ThreadLocalHolder.getThreadLocal().get();
        final var userId = userInfo.getUserId();
        final var userAccount = userAccountRepository.getUserById(userId);
        if (!userAccount.isAdmin()) {
            log.error(NOT_AUTHORIZED_TO_ALTER_WEBHOOKS_MESSAGE);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } else {
            final SubscriptionInformation subscriptionInformation = stravaActivityService.getWebhookSubscription();
            return ResponseEntity.ok(subscriptionInformation);
        }
    }
}

