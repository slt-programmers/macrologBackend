package slt.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import slt.config.StravaConfig;
import slt.connectivity.strava.StravaActivityService;
import slt.connectivity.strava.dto.SubscriptionInformation;
import slt.connectivity.strava.dto.WebhookEvent;
import slt.database.SettingsRepository;
import slt.database.UserAccountRepository;
import slt.database.WeightRepository;
import slt.database.entities.Setting;
import slt.database.entities.UserAccount;
import slt.database.entities.Weight;
import slt.dto.*;
import slt.security.ThreadLocalHolder;
import slt.security.UserInfo;
import slt.util.LocalDateParser;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/webhooks")
@Api(value = "webhooks")
public class WebHookService {

    @Autowired
    private StravaActivityService stravaActivityService;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private StravaConfig stravaConfig;

    @ApiOperation(value = "Handle a webhook event from Strava")
    @PostMapping(path = "public/strava", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity syncStrava(@RequestBody WebhookEvent event) {

        stravaActivityService.receiveWebHookEvent(event);
        return ResponseEntity.ok().build();
    }

    @ApiOperation(value = "Handle a challenge request from Strava")
    @GetMapping(path = "public/strava", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity syncStravaCallback(@RequestParam(name = "hub.mode") String hubMode,
                                             @RequestParam(name = "hub.challenge") String hubChallenge,
                                             @RequestParam(name = "hub.verify_token") String hubVerifyToken) {

        log.debug("Received strava callback {} {} {}", hubMode, hubChallenge, hubVerifyToken);
        if (!"subscribe".equals(hubMode)|| !stravaConfig.getVerifytoken().equals(hubVerifyToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } else {
            String ret = "{\"hub.challenge\":\"" + hubChallenge + "\"}";
            return ResponseEntity.ok(ret);
        }
    }

    @ApiOperation(value = "Start a webhook subscription with Strava")
    @PostMapping(path = "/webhook/STRAVA", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SubscriptionInformation> startWebhook() {
        UserInfo userInfo = ThreadLocalHolder.getThreadLocal().get();
        Integer userId = userInfo.getUserId();
        UserAccount userAccount = userAccountRepository.getUserById(userId);
        if (!userAccount.isAdmin()) {
            log.error("Not authorized to alter webhooks");
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        } else {
            final SubscriptionInformation subscriptionInformation = stravaActivityService.startWebhookSubcription();
            return ResponseEntity.ok(subscriptionInformation);
        }
    }

    @ApiOperation(value = "Delete a webhook subscription with Strava")
    @DeleteMapping(path = "/webhook/STRAVA/{subscriptionId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity endWebhook(@PathVariable("subscriptionId") Integer subscriptionId) {
        UserInfo userInfo = ThreadLocalHolder.getThreadLocal().get();
        Integer userId = userInfo.getUserId();
        UserAccount userAccount = userAccountRepository.getUserById(userId);
        if (!userAccount.isAdmin()) {
            log.error("Not authorized to alter webhooks");
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        } else {

            stravaActivityService.endWebhookSubscription(subscriptionId);
            return ResponseEntity.ok().build();
        }
    }

    @ApiOperation(value = "Retrieve the webhook information with Strava")
    @GetMapping(path = "/webhook/STRAVA", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SubscriptionInformation> getWebhook() {
        UserInfo userInfo = ThreadLocalHolder.getThreadLocal().get();
        Integer userId = userInfo.getUserId();
        UserAccount userAccount = userAccountRepository.getUserById(userId);
        if (!userAccount.isAdmin()) {
            log.error("Not authorized to alter webhooks");
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        } else {
            final SubscriptionInformation subscriptionInformation = stravaActivityService.getWebhookSubscription();
            return ResponseEntity.ok(subscriptionInformation);
        }
    }
}

