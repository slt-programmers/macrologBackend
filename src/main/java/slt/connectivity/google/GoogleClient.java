package slt.connectivity.google;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Base64;
import com.google.api.services.gmail.Gmail;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import slt.config.GoogleConfig;
import slt.connectivity.google.dto.Oath2Token;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

@Slf4j
@Component
@AllArgsConstructor
public class GoogleClient {

    private static final String CLIENT_ID = "client_id";
    private static final String CLIENT_SECRET = "client_secret";
    private static final String GRANT_TYPE = "grant_type";
    private static final String TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final String ERROR_MESSAGE = "Fout bij versturen.";

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private GoogleConfig googleConfig;

    public void sendMail(Oath2Token oath2Token, Message message)
            throws IOException, GeneralSecurityException {
        if (oath2Token == null) {
            log.error("Unable to send mail. No token available");
            return;
        }

        final var credential = new GoogleCredential().setAccessToken(oath2Token.getAccess_token());
        final var httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        final var jsonFactory = JacksonFactory.getDefaultInstance();
        try {
            com.google.api.services.gmail.model.Message googleMessage = createMessageWithEmail(message);
            final Gmail service = new Gmail.Builder(httpTransport, jsonFactory, credential)
                    .setApplicationName(googleConfig.getApplicationName())
                    .build();
            final com.google.api.services.gmail.model.Message execute = service
                    .users()
                    .messages()
                    .send("me", googleMessage)
                    .execute();
            log.debug("Mail {} send.", execute.getId());
        } catch (MessagingException e) {
            log.error("Error during sending mail", e);
        }
    }

    public MimeMessage createEmail(String to,
                                   String from,
                                   String subject,
                                   String bodyText)
            throws MessagingException {
        final var props = new Properties();
        final var session = Session.getDefaultInstance(props, null);
        final var email = new MimeMessage(session); // NOSONAR

        email.setFrom(new InternetAddress(from));
        email.addRecipient(javax.mail.Message.RecipientType.TO, new InternetAddress(to));
        email.setSubject(subject);
        email.setContent(bodyText, "text/html");
        return email;
    }

    protected com.google.api.services.gmail.model.Message createMessageWithEmail(final Message emailContent)
            throws MessagingException, IOException {
        final var buffer = new ByteArrayOutputStream();
        emailContent.writeTo(buffer);
        byte[] bytes = buffer.toByteArray();
        final var encodedEmail = Base64.encodeBase64URLSafeString(bytes);
        final var message = new com.google.api.services.gmail.model.Message();
        message.setRaw(encodedEmail);
        return message;
    }

    public Optional<Oath2Token> getAuthorizationToken(final String authorizationCode) {
        final var clientId = googleConfig.getClientId();
        final var clientSecret = googleConfig.getClientSecret();

        final var reqPayload = new HashMap<String, String>();
        reqPayload.put(CLIENT_ID, clientId);
        reqPayload.put(CLIENT_SECRET, clientSecret);
        reqPayload.put("code", authorizationCode);
        reqPayload.put(GRANT_TYPE, "authorization_code");
        reqPayload.put("redirect_uri", googleConfig.getRedirectUri());

        return getAuthorizationToken(reqPayload);
    }

    public Optional<Oath2Token> refreshToken(final String refreshToken) {
        final var grantType = "refresh_token";
        final var clientId = googleConfig.getClientId();
        final var clientSecret = googleConfig.getClientSecret();

        final var reqPayload = new HashMap<String, String>();
        reqPayload.put(CLIENT_ID, clientId);
        reqPayload.put(CLIENT_SECRET, clientSecret);
        reqPayload.put("refresh_token", refreshToken);
        reqPayload.put(GRANT_TYPE, grantType);

        return getAuthorizationToken(reqPayload);
    }

    private Optional<Oath2Token> getAuthorizationToken(final Map<String, String> reqPayload) {
        try {
            final HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            final var entity = new HttpEntity<>(reqPayload, headers);
            final var responseEntity = restTemplate.exchange(TOKEN_URL, HttpMethod.POST, entity, Oath2Token.class);

            return Optional.of(responseEntity.getBody());
        } catch (HttpClientErrorException httpClientErrorException) {
            log.error(httpClientErrorException.getResponseBodyAsString());
            log.error(ERROR_MESSAGE + " {}", httpClientErrorException.getLocalizedMessage(), httpClientErrorException);
            return Optional.empty();
        } catch (RestClientException restClientException) {
            log.error(ERROR_MESSAGE + " {}", restClientException.getLocalizedMessage(), restClientException);
            return Optional.empty();
        }
    }
}
