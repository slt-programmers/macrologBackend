package slt.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import slt.config.GoogleConfig;
import slt.connectivity.google.GoogleClient;
import slt.connectivity.google.dto.Oath2Token;
import slt.database.SettingsRepository;
import slt.database.entities.Setting;
import slt.database.entities.UserAccount;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class GoogleMailServiceTest {

    private SettingsRepository settingsRepository;
    private GoogleConfig googleConfig;
    private GoogleClient googleClient;

    private GoogleMailService googleMailService;

    @BeforeEach
    void beforEach() {
        settingsRepository = mock(SettingsRepository.class);
        googleConfig = mock(GoogleConfig.class);
        googleClient = mock(GoogleClient.class);
        googleMailService = new GoogleMailService(settingsRepository, googleConfig, googleClient);
    }

    @Test
    void mailStatusStaatUit() {
        when(googleConfig.getClientSecret()).thenReturn("uit");
        final var mailService = new GoogleMailService(settingsRepository, googleConfig, googleClient);
        final var mailStatus = mailService.getMailStatus();
        Assertions.assertFalse(mailStatus.isConnected());
    }

    @Test
    void getMailStatusUitMaarGeenSetting() {
        when(googleConfig.getClientSecret()).thenReturn("a");
        when(settingsRepository.getLatestSetting(any(), any())).thenReturn(null);
        final var mailService = new GoogleMailService(settingsRepository, googleConfig, googleClient);
        final var mailStatus = mailService.getMailStatus();
        Assertions.assertFalse(mailStatus.isConnected());
    }

    @Test
    void getMailStatusAanMetSetting() {
        when(googleConfig.getClientSecret()).thenReturn("a");
        when(settingsRepository.getLatestSetting(any(), any())).thenReturn(Setting.builder().build());
        final var mailService = new GoogleMailService(settingsRepository, googleConfig, googleClient);
        final var mailStatus = mailService.getMailStatus();
        Assertions.assertTrue(mailStatus.isConnected());
    }

    @Test
    void registerWithCodeNoSetting() {
        when(googleClient.getAuthorizationToken(eq("code"))).thenReturn(Optional.empty());
        googleMailService.registerWithCode("code");
        verify(settingsRepository, times(1)).putSetting(any());
    }

    @Test
    void registerWithCodeWithSetting() {
        when(googleConfig.getClientSecret()).thenReturn("s");
        Assertions.assertFalse(googleMailService.getMailStatus().isConnected());
        when(googleClient.getAuthorizationToken(eq("code"))).thenReturn(Optional.of(Oath2Token.builder().expires_in(200L).build()));
        googleMailService.registerWithCode("code");
        Assertions.assertTrue(googleMailService.getMailStatus().isConnected());
        verify(settingsRepository, times(4)).putSetting(any());
    }

    @Test
    void sendPasswordRetrievalMail() throws MessagingException, IOException, GeneralSecurityException {
        when(googleConfig.getClientSecret()).thenReturn("secret");
        final var mimemessage = mock(MimeMessage.class);
        when(googleClient.createEmail(any(), any(), any(), any())).thenReturn(mimemessage);

        // Niet expired token:
        final var instant = Instant.now().plus(20, ChronoUnit.MINUTES);
        when(settingsRepository.getLatestSetting(eq(-1L), any())).thenReturn(Setting.builder().value(String.valueOf(instant.getEpochSecond())).build());
        final var mailService = new GoogleMailService(settingsRepository, googleConfig, googleClient);
        mailService.sendPasswordRetrievalMail("mail", null, UserAccount.builder().build());
        verify(settingsRepository, times(5)).getLatestSetting(any(), any());
        verify(googleClient, times(1)).sendMail(any(Oath2Token.class), any(MimeMessage.class));
    }

    @Test
    void sendConfirmationMail() throws MessagingException, IOException, GeneralSecurityException {
        when(googleConfig.getClientSecret()).thenReturn("secret");
        final var mimemessage = mock(MimeMessage.class);
        when(googleClient.createEmail(any(), any(), any(), any())).thenReturn(mimemessage);

        // Niet expired token:
        final var instant = Instant.now().plus(20, ChronoUnit.MINUTES);
        when(settingsRepository.getLatestSetting(eq(-1L), any())).thenReturn(Setting.builder().value(String.valueOf(instant.getEpochSecond())).build());
        final var mailService = new GoogleMailService(settingsRepository, googleConfig, googleClient);
        mailService.sendConfirmationMail("mail", UserAccount.builder().build());
        verify(settingsRepository, times(5)).getLatestSetting(any(), any());
        verify(googleClient, times(1)).sendMail(any(Oath2Token.class), any(MimeMessage.class));
    }

    @Test
    void sendTestMail() throws MessagingException, IOException, GeneralSecurityException {
        when(googleConfig.getClientSecret()).thenReturn("secret");
        final var mimemessage = mock(MimeMessage.class);
        when(googleClient.createEmail(any(), any(), any(), any())).thenReturn(mimemessage);

        // Niet expired token:
        final var instant = Instant.now().plus(20, ChronoUnit.MINUTES);
        when(settingsRepository.getLatestSetting(eq(-1L), any())).thenReturn(Setting.builder().value(String.valueOf(instant.getEpochSecond())).build());
        final var mailService = new GoogleMailService(settingsRepository, googleConfig, googleClient);
        mailService.sendTestMail("mail");
        verify(settingsRepository, times(5)).getLatestSetting(any(), any());
        verify(googleClient, times(1)).sendMail(any(Oath2Token.class), any(MimeMessage.class));
    }
}