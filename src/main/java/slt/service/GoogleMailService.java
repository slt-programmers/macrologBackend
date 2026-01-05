package slt.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import slt.config.GoogleConfig;
import slt.connectivity.google.GoogleClient;
import slt.connectivity.google.dto.Oath2Token;
import slt.database.SettingsRepository;
import slt.database.entities.Setting;
import slt.database.entities.UserAccount;
import slt.dto.ConnectivityStatusDto;

import jakarta.transaction.Transactional;

import java.sql.Date;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

@Service
@Slf4j
public class GoogleMailService {

    private static final String GMAIL_CLIENT_AUTHORIZATION_CODE = "GMAIL_CLIENT_AUTHORIZATION_CODE";
    private static final String GMAIL_ACCESS_TOKEN = "GMAIL_ACCESS_TOKEN";
    private static final String GMAIL_EXPIRES_AT = "GMAIL_EXPIRES_AT";
    private static final String GMAIL_REFRESH_TOKEN = "GMAIL_REFRESH_TOKEN";
    private static final String MACROLOG_FROM_ADDRESS = "macrologwebapp@gmail.com";
    private static final String MAIL_SEND_TO_DEBUGLINE = "Mail send to";
    private static final Long ADMIN_USER_ID = -1L;

    private final SettingsRepository settingsRepository;
    private final GoogleConfig googleConfig;
    private final GoogleClient googleClient;

    private Boolean connected;

    public GoogleMailService(final SettingsRepository settingsRepository,
                             final GoogleConfig googleConfig,
                             final GoogleClient googleClient) {
        this.settingsRepository = settingsRepository;
        this.googleConfig = googleConfig;
        this.googleClient = googleClient;
        if (googleConfig == null || "UIT".equalsIgnoreCase(googleConfig.getClientSecret())) {
            log.info("Google Mail turned off");
            this.connected = false;
        } else {
            log.debug("Setting up Mail");
            this.connected = (settingsRepository.getLatestSetting(ADMIN_USER_ID, GMAIL_ACCESS_TOKEN) != null);
            log.debug("Status Google Mail {}", this.connected);
        }
    }

    public ConnectivityStatusDto getMailStatus() {
        return ConnectivityStatusDto.builder()
                .connected(this.connected)
                .syncedApplicationId(googleConfig.getClientId())
                .build();
    }

    public void registerWithCode(final String clientAuthorizationCode) {
        final var setting = Setting.builder()
                .userId(ADMIN_USER_ID)
                .name(GMAIL_CLIENT_AUTHORIZATION_CODE)
                .value(clientAuthorizationCode)
                .day(Date.valueOf(LocalDate.now()))
                .build();
        settingsRepository.putSetting(setting);

        final var optionalToken = googleClient.getAuthorizationToken(clientAuthorizationCode);
        if (optionalToken.isPresent()) {
            final var token = optionalToken.get();
            final var expiresIn = Long.valueOf(token.getExpires_in().toString());
            final long expiresAt = getExpiresAtFromExpiresIn(expiresIn);

            saveSetting(GMAIL_ACCESS_TOKEN, token.getAccess_token());
            saveSetting(GMAIL_REFRESH_TOKEN, token.getRefresh_token());
            saveSetting(GMAIL_EXPIRES_AT, String.valueOf(expiresAt));
            connected = true;
            log.info("Connected to Google!");
        } else {
            log.error("Unable to get token for gmail");
        }
    }


    public void sendPasswordRetrievalMail(final String email, final String unhashedTemporaryPassword, final UserAccount account) {
        if (isConnnectedToGmail()) {
            try {
                final var subject = "Macrolog Credentials";
                final var body = "<h3>Hello " + account.getUserName() + ", </h3>" +
                        "<p>A request has been made to reset your password. </p>" +
                        "<p>We have generated a new password for you: <i>" + unhashedTemporaryPassword + "</i>. </p>" +
                        "<p>You can use this within 30 minutes to log in and choose a new password of your own. </p>" +
                        "<p>If you did not request this password change, you can ignore this messsage. </p>" +
                        "<p>See you soon! </p>" +
                        "<p>Carmen and Arjan from Macrolog </p>";

                log.debug(MAIL_SEND_TO_DEBUGLINE + email);
                final var email1 = googleClient.createEmail(email, MACROLOG_FROM_ADDRESS, subject, body);
                final var optionalToken = getOath2Token();
                assert optionalToken.isPresent();
                final var token = optionalToken.get();
                googleClient.sendMail(token, email1);
            } catch (Exception ex) {
                log.error(ex.getMessage());
            }
        }
    }

    public void sendConfirmationMail(final String email, final UserAccount account) {
        if (isConnnectedToGmail()) {
            try {
                final var subject = "Welcome to Macrolog!";
                final var body = "<p>Hello " + account.getUserName() + ", </p>" +
                        "<p>Thank you for using Macrolog!</p>" +
                        "<p>You are now ready to use both the app and the <a href=\"https://macrolog.herokuapp.com/\"> website</a>. " +
                        "Our aim is to make it as easy as possible to log your food intake on a daily basis. " +
                        "We hope this app ultimately helps you to achieve your goals, whatever they may be. </p>" +
                        "<p>All the best,</p>" +
                        "<p>Carmen and Arjan from Macrolog</p>";

                log.debug(MAIL_SEND_TO_DEBUGLINE + email);
                final var email1 = googleClient.createEmail(email, MACROLOG_FROM_ADDRESS, subject, body);
                final var optionalToken = getOath2Token();
                assert optionalToken.isPresent();
                final var token = optionalToken.get();
                googleClient.sendMail(token, email1);
            } catch (Exception ex) {
                log.error(ex.getMessage());
            }
        }
    }

    public void sendTestMail(final String email) {
        if (isConnnectedToGmail()) {
            try {
                final var subject = "Test mail from Macrolog!";
                final var body = "<p>Hello,</p>" +
                        "<p>This is a testmail for Macrolog!</p>" +
                        "<p>And it works! Yay! </p>" +
                        "<p>All the best,</p>" +
                        "<p>Carmen and Arjan from Macrolog</p>";

                log.debug(MAIL_SEND_TO_DEBUGLINE + email);
                final var email1 = googleClient.createEmail(email, MACROLOG_FROM_ADDRESS, subject, body);
                final var optionalToken = getOath2Token();
                assert optionalToken.isPresent();
                final var token = optionalToken.get();
                googleClient.sendMail(token, email1);
            } catch (Exception ex) {
                log.error(ex.getMessage());
            }
        }
    }

    private boolean isConnnectedToGmail() {
        if (!connected) {
            log.error("Uable to send mail. Google Mail is not connected.'");
            return false;
        }
        return true;
    }

    private long getExpiresAtFromExpiresIn(final Long expiresIn) {
        final var instant = Instant.now();
        return instant.plusSeconds(expiresIn).getEpochSecond();
    }

    private void saveSetting(final String name, final String value) {
        settingsRepository.putSetting(Setting.builder()
                .userId(GoogleMailService.ADMIN_USER_ID)
                .name(name)
                .value(value)
                .day(Date.valueOf(LocalDate.now())).build());
    }

    private Optional<Oath2Token> getOath2Token() {
        final var accessToken = settingsRepository.getLatestSetting(GoogleMailService.ADMIN_USER_ID, GMAIL_ACCESS_TOKEN);
        final var refreshToken = settingsRepository.getLatestSetting(GoogleMailService.ADMIN_USER_ID, GMAIL_REFRESH_TOKEN);
        final var expiresAt = settingsRepository.getLatestSetting(GoogleMailService.ADMIN_USER_ID, GMAIL_EXPIRES_AT);

        if (accessToken == null ||
                refreshToken == null ||
                expiresAt == null) {
            log.error("Gmail session not initialized");
            return Optional.empty();
        }

        final var token = Oath2Token.builder()
                .access_token(accessToken.getValue())
                .refresh_token(refreshToken.getValue())
                .expires_at(Long.valueOf(expiresAt.getValue()))
                .build();

        if (isExpired(token)) {
            log.debug("Token is expired. Refreshing..");
            final var optionalToken = googleClient.refreshToken(token.getRefresh_token());
            if (optionalToken.isEmpty()) {
                log.error("Unable to get new token");
                return Optional.empty();
            } else {
                final var newToken = optionalToken.get();
                if (isExpired(newToken)) {
                    log.error("New token also expired. wtf...");
                    return Optional.empty();
                } else {
                    storeTokenSettings(newToken);
                    return Optional.of(newToken);
                }
            }
        } else {
            return Optional.of(token);
        }
    }

    @Transactional
    private void storeTokenSettings(final Oath2Token oath2Token) {
        log.debug("Storing token update");

        final var accessToken = settingsRepository.getLatestSetting(GoogleMailService.ADMIN_USER_ID, GMAIL_ACCESS_TOKEN);
        final var refreshToken = settingsRepository.getLatestSetting(GoogleMailService.ADMIN_USER_ID, GMAIL_REFRESH_TOKEN);
        final var expireAt = settingsRepository.getLatestSetting(GoogleMailService.ADMIN_USER_ID, GMAIL_EXPIRES_AT);

        accessToken.setValue(oath2Token.getAccess_token());
        if (StringUtils.isNotEmpty(oath2Token.getRefresh_token())) {
            refreshToken.setValue(oath2Token.getRefresh_token());
        }
        if (oath2Token.getExpires_at() == null) {
            oath2Token.setExpires_at(getExpiresAtFromExpiresIn(oath2Token.getExpires_in()));
        }
        expireAt.setValue(oath2Token.getExpires_at().toString());

        settingsRepository.saveSetting(GoogleMailService.ADMIN_USER_ID, accessToken);
        settingsRepository.saveSetting(GoogleMailService.ADMIN_USER_ID, refreshToken);
        settingsRepository.saveSetting(GoogleMailService.ADMIN_USER_ID, expireAt);
    }

    private boolean isExpired(final Oath2Token token) {
        var expiresAt = token.getExpires_at();
        if (expiresAt == null) {
            expiresAt = getExpiresAtFromExpiresIn(token.getExpires_in());
            token.setExpires_at(expiresAt);
        }
        final var instant = Instant.ofEpochSecond(expiresAt);
        final var timeTokenExpires = instant.atZone(ZoneId.systemDefault()).toLocalDateTime();
        final var currentTime = LocalDateTime.now(ZoneId.systemDefault()).plusMinutes(10);
        log.debug("Token valid until [{}]", timeTokenExpires);
        return timeTokenExpires.isBefore(currentTime);
    }


}