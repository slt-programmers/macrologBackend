package slt.notification;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Base64;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import slt.service.GoogleMailService;
import slt.connectivity.strava.StravaToken;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.*;
import java.security.GeneralSecurityException;
import java.util.*;

@Slf4j
public class GMailTest {


    public static com.google.api.services.gmail.model.Message createMessageWithEmail(MimeMessage emailContent)
            throws MessagingException, IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        emailContent.writeTo(buffer);
        byte[] bytes = buffer.toByteArray();
        String encodedEmail = Base64.encodeBase64URLSafeString(bytes);
        com.google.api.services.gmail.model.Message message = new com.google.api.services.gmail.model.Message();
        message.setRaw(encodedEmail);
        return message;
    }

    public static MimeMessage createEmail(String to,
                                          String from,
                                          String subject,
                                          String bodyText)
            throws MessagingException {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);

        MimeMessage email = new MimeMessage(session);

        email.setFrom(new InternetAddress(from));
        email.addRecipient(javax.mail.Message.RecipientType.TO,
                new InternetAddress(to));
        email.setSubject(subject);
        email.setText(bodyText);
        return email;
    }

    public static void main2(String... args) throws IOException, GeneralSecurityException {
        // Build a new authorized API client service.

        final String serviceAccountId = "macrologmailer@macrolog.iam.gserviceaccount.com";
        String fromEmail = "macrologwebapp@gmail.com";

        com.google.api.services.gmail.model.Message message = null;
        try {
            message = createMessageWithEmail(createEmail("arjan.tienkamp@gmail.com", fromEmail, "test", "test"));
        } catch (MessagingException e) {
            e.printStackTrace();
        }


        String jsonFile = "/Macrolog-mailer.json";
        InputStream in = GoogleMailService.class.getResourceAsStream(jsonFile);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + jsonFile);
        }
//        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));


//       if (false) {
//           final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
//           Gmail service = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
//                   .setApplicationName(APPLICATION_NAME)
//                   .build();
//
//
//           // Print the labels in the user's account.
//           String user = "me";
//           com.google.api.services.gmail.model.Message message = null;
//           try {
//               message = createMessageWithEmail(createEmail("arjant.tienkamp@gmail.com", user, "test", "test"));
//           } catch (MessagingException e) {
//               e.printStackTrace();
//           }
//           final com.google.api.services.gmail.model.Message execute = service.users().messages().send("me", message).execute();
//           System.out.println("Message id: " + execute.getId());
//           System.out.println(execute.toPrettyString());
//       }
//
        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
//        String serviceAccount = "macrologwebserver@macrolog.iam.gserviceaccount.com";
        //    String serviceAccount = "macrologmailer@macrolog.iam.gserviceaccount.com";
        GoogleCredential credential = GoogleCredential.fromStream(in)
                .createScoped(GmailScopes.all());//.createDelegated("macrologwebapp@gmail.com");

        GoogleCredential.Builder builder = new GoogleCredential.Builder()
                .setTransport(httpTransport)
                .setJsonFactory(jsonFactory)
                .setServiceAccountProjectId("macrolog")
                .setServiceAccountScopes(GmailScopes.all())
                .setServiceAccountId(serviceAccountId)
                .setServiceAccountPrivateKey(credential.getServiceAccountPrivateKey())
                .setServiceAccountPrivateKeyId(credential.getServiceAccountPrivateKeyId())
//                .setTokenServerEncodedUrl(credential.getTokenServerEncodedUrl())
                .setServiceAccountUser(serviceAccountId);

        final GoogleCredential delegated = builder.build();//.createDelegated(fromEmail);
        final Gmail service = new Gmail.Builder(httpTransport, jsonFactory, delegated).setApplicationName("Macrolog").build();

        final com.google.api.services.gmail.model.Message execute = service.users().messages().send(serviceAccountId, message).execute();
//        final Gmail.Users.GetProfile execute = service.users().getProfile("me");

        //      System.out.println(execute.execute().getEmailAddress());


    }

    private static final java.io.File DATA_STORE_DIR =
            new java.io.File(System.getProperty("user.home"), ".store/oauth2_sample");

    private static FileDataStoreFactory dataStoreFactory;


    public static final String CLIENT_ID = "client_id";
    public static final String CLIENT_SECRET = "client_secret";
    public static final String CALLBACK_URL = "callback_url";
    public static final String VERIFY_TOKEN = "verify_token";
    public static final String GRANT_TYPE = "grant_type";


    public static void grantApp() throws IOException, GeneralSecurityException {
        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();


        String clientId = "164533152729-44ujqq61ja6v2563uaargf1nc00hfoou.apps.googleusercontent.com";
        String project_id ="macrolog-localhost";
        String client_secret=  "m6RFdjxHCPpjHsh99uA6IEaM";
        String grantType = "authorization_code";

//        String clientId = stravaConfig.getClientId().toString();
//        String clientSecret = stravaConfig.getClientSecret();
//        Map reqPayload = new HashMap();
//        reqPayload.put(CLIENT_ID, clientId);
//        reqPayload.put(CLIENT_SECRET, client_secret);
//        reqPayload.put("code", client_secret);
//        reqPayload.put(GRANT_TYPE, grantType);

//        final StravaToken token = getToken(reqPayload);

//        if (true) return;
        /*
        "client_id": "164533152729-44ujqq61ja6v2563uaargf1nc00hfoou.apps.googleusercontent.com",
                "project_id": "macrolog-localhost",
                "client_secret": "m6RFdjxHCPpjHsh99uA6IEaM",
*/

        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(jsonFactory,
                new InputStreamReader(GMailTest.class.getResourceAsStream("/client_google.json")));
        clientSecrets.getInstalled().setClientId(clientId);
        clientSecrets.getInstalled().set("project_id", project_id);
        clientSecrets.getInstalled().setClientSecret(client_secret);


        dataStoreFactory = new FileDataStoreFactory(DATA_STORE_DIR);
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, jsonFactory, clientSecrets, GmailScopes.all()).setDataStoreFactory(
                dataStoreFactory).build();
        final Credential authorize = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("macrologwebapp@gmail.com");
        final String accessToken = authorize.getAccessToken();
        final String refreshToken = authorize.getRefreshToken();
        final Long expiresInSeconds = authorize.getExpiresInSeconds();

        log.info("ACCESSTOKEN [{}]", accessToken);
        log.info("REFRESHTOKEN [{}]", refreshToken);
        log.info("expiresInSeconds [{}]", expiresInSeconds);

        /*
14:29:09.751 [main] INFO  slt.notification.GMailTest - ACCESSTOKEN [ya29.Il-bB0R6j2IxOapx6GSq8wBfP2jZ1eDAuqwC3RGYFmdty1uew-a-aw0OU8pU6fgbn0vabMK65TK9tLbvF5E6gTGvYSN5XLTETbknhiYlH5Qnn0Nyd_wFLFCH5TJl6y4-jg]
14:29:09.752 [main] INFO  slt.notification.GMailTest - REFRESHTOKEN [1/AtIhNlNyz7lkuwdBOCUYgTAl2Y_MZpOF62ILNWK4ymw]
14:29:09.752 [main] INFO  slt.notification.GMailTest - expiresInSeconds [3599]
         */


    }


    private static  StravaToken getToken(Map reqPayload) {
        try {
            final HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            final HttpEntity<HashMap> entity = new HttpEntity(reqPayload, headers);
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<StravaToken> responseEntity = restTemplate.exchange("https://accounts.google.com/o/oauth2/auth", HttpMethod.POST, entity, StravaToken.class);

            return responseEntity.getBody();

        } catch (HttpClientErrorException httpClientErrorException) {
            log.error(httpClientErrorException.getResponseBodyAsString());
            log.error("ERROR" + " {}", httpClientErrorException.getLocalizedMessage(), httpClientErrorException);
            return null;
        } catch (RestClientException restClientException) {
            log.error("ERROR" + " {}", restClientException.getLocalizedMessage(), restClientException);
            return null;
        }
    }

    public static void sendMail(StravaToken stravaToken) throws IOException, GeneralSecurityException {
        String fromEmail = "macrologwebapp@gmail.com";

        com.google.api.services.gmail.model.Message message = null;
        try {
            message = createMessageWithEmail(createEmail("arjan.tienkamp@gmail.com", fromEmail, "test", "test"));
        } catch (MessagingException e) {
            e.printStackTrace();
        }

        GoogleCredential credential = new GoogleCredential().setAccessToken(stravaToken.getAccess_token());

        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();


        final Gmail service = new Gmail.Builder(httpTransport, jsonFactory, credential).setApplicationName("macrolog").build();
        final com.google.api.services.gmail.model.Message execute = service.users().messages().send("me", message).execute();

    }

    private static void refresh(StravaToken stravaToken) throws IOException, GeneralSecurityException {


        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(jsonFactory,
                new InputStreamReader(GMailTest.class.getResourceAsStream("/client_secret.json")));

        GoogleCredential.Builder builder = new GoogleCredential.Builder()
                .setTransport(httpTransport).setClientSecrets(clientSecrets)
                .setJsonFactory(jsonFactory);

        GoogleCredential credential = builder.build()
                .setRefreshToken(stravaToken.getRefresh_token())
                .setAccessToken(stravaToken.getAccess_token());
        final boolean refreshed = credential.refreshToken();

        log.debug("refreshed {}", refreshed);
        log.info("ACCESSTOKEN [{}]", credential.getAccessToken());
        log.info("REFRESHTOKEN [{}]", credential.getRefreshToken());
    }

    public static void main(String... args) throws IOException, GeneralSecurityException {


        final StravaToken build = StravaToken.builder()
                .access_token("ya29.Il-bB0R6j2IxOapx6GSq8wBfP2jZ1eDAuqwC3RGYFmdty1uew-a-aw0OU8pU6fgbn0vabMK65TK9tLbvF5E6gTGvYSN5XLTETbknhiYlH5Qnn0Nyd_wFLFCH5TJl6y4-jg")
                .refresh_token("1/AtIhNlNyz7lkuwdBOCUYgTAl2Y_MZpOF62ILNWK4ymw")
                .build();

//        String clientId = "191982532326-m8cu85jse02fejcdf432ei2cnj93av4g.apps.googleusercontent.com";
        String clientId = "164533152729-44ujqq61ja6v2563uaargf1nc00hfoou.apps.googleusercontent.com";
 //       String clientGeheim = "wR9BgXrRtxugJtHbBPHlHgLs";

        if (true) {
              log.info(getOauthUrlGoogle(clientId));
// redirect naar http://localhost:1234/googlecallbacl/oauth2callback?state=f803acb0-13e8-465a-80e4-bedba5ab0d36&code=4%2FsAEgNDIdE4KWlYIjzMBJCiSCKZDqls0ZT4321ZSkrQlNu2BUg6SLHhXfgL5Hap-nopICY9XRIU4tBF4Pc5L5HC0&scope=email+profile+https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fuserinfo.profile+https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fuserinfo.email+openid&authuser=1&session_state=66a45843575a4d5c496c7edc175a277e8423f692..871b&prompt=consent
// error:
// http://localhost:1234/googlecallbacl/oauth2callback?error=access_denied&state=56deeaf1-81bd-421a-be0f-6bfe618d0f67

              //            sendMail(build);
//            grantApp();
//            refresh(build);
        }
        if (true) {
            return;
        }


//        String clientId = "191982532326-m8cu85jse02fejcdf432ei2cnj93av4g.apps.googleusercontent.com";
//        String clientGeheim = "wR9BgXrRtxugJtHbBPHlHgLs";

//        String accessToken  ="114296103478534510562";
//        GoogleCredential credential = new GoogleCredential().setAccessToken(accessToken);
        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(jsonFactory,
                new InputStreamReader(GMailTest.class.getResourceAsStream("/client_secret.json")));

        dataStoreFactory = new FileDataStoreFactory(DATA_STORE_DIR);
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, jsonFactory, clientSecrets, GmailScopes.all()).setDataStoreFactory(
                dataStoreFactory).build();
/*
        {
            "access_token": "ya29.Il-bB6WJ7kYG3PlstOfY-f7oyZfb9XdP1pxl9wAlvFjzrhlate7-wy5UMKMzKqhZA7FkdhfXGKc1zy6B6bN5k0gznimiGizapxXgbiegIGtl-1NX57kdrobXpcyL8OhoBQ",
                "scope": "https://www.googleapis.com/auth/gmail.send https://www.googleapis.com/auth/gmail.compose https://www.googleapis.com/auth/gmail.metadata https://www.googleapis.com/auth/gmail.settings.sharing https://www.googleapis.com/auth/gmail.modify https://www.googleapis.com/auth/gmail.readonly https://www.googleapis.com/auth/gmail.settings.basic https://mail.google.com/ https://www.googleapis.com/auth/gmail.labels https://www.googleapis.com/auth/gmail.insert",
                "token_type": "Bearer",
                "expires_in": 3600,
                "refresh_token": "1//04Frh7SjOSun0CgYIARAAGAQSNwF-L9IrgoIJKiL8mhdDDeXXGCmvwGclF1trXx7dae2SgOGwcHH3RlWKuHRZm58ErZd8CNOpdYY"
        }
*/
//        final Credential authorize = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("macrologwebapp@gmail.com");
//
//        final String accessToken = authorize.getAccessToken();
//
        String auhtorizationCode = "4/sAHyskzwHFFGQy4H9bHV2olwcr9BGy3qID98VSt7WDFBBnqhsujSgzoa7sWn0G18s-JM_jsHOG5JZoRZzukm7_M";
//        final GoogleAuthorizationCodeTokenRequest googleAuthorizationCodeTokenRequest = flow.newTokenRequest(auhtorizationCode);

        //      googleAuthorizationCodeTokenRequest.

////        final GoogleTokenResponse execute1 = googleAuthorizationCodeTokenRequest.execute();
//        final String accessToken1 = execute1.getAccessToken();
//        final String refreshToken = execute1.getRefreshToken();
//

        String accessToken1 = "ya29.Il-bB6WJ7kYG3PlstOfY-f7oyZfb9XdP1pxl9wAlvFjzrhlate7-wy5UMKMzKqhZA7FkdhfXGKc1zy6B6bN5k0gznimiGizapxXgbiegIGtl-1NX57kdrobXpcyL8OhoBQ";
        String refreshToken = "1//04Frh7SjOSun0CgYIARAAGAQSNwF-L9IrgoIJKiL8mhdDDeXXGCmvwGclF1trXx7dae2SgOGwcHH3RlWKuHRZm58ErZd8CNOpdYY";

        log.info("ACCESSTOKEN [{}]", accessToken1);
        log.info("REFRESHTOKEN [{}]", refreshToken);

        GoogleCredential credential = new GoogleCredential().setAccessToken(accessToken1);


        final Gmail service = new Gmail.Builder(httpTransport, jsonFactory, credential).setApplicationName("macrolog").build();
        final ListMessagesResponse execute = service.users().messages().list("me").execute();
        final List<Message> messages = execute.getMessages();

    }

    public static String getOauthUrlGoogle(String clientId) {
//        String redirectUrl = "http://www.tralev.com/web/oauth2callback";
//        if (Html.isRunningLocally()) {
//            redirectUrl = "http://localhost:8080/TralevServer/web/oauth2callback";
//        }
        String redirectUrl = "http://localhost:1234/googlecallbacl/oauth2callback";

        StringBuilder oauthUrl = new StringBuilder();
        oauthUrl.append("https://accounts.google.com/o/oauth2/auth")
                .append("?client_id=").append(clientId) // the client id from the api console registration
                .append("&response_type=code")
                .append("&scope="+GmailScopes.GMAIL_SEND.toString()) // scope is the api permissions we are requesting
                .append("&redirect_uri=").append(redirectUrl) // the servlet that google redirects to after authorization
                .append("&state=").append(UUID.randomUUID().toString())
                //.append("&access_type=offline") // here we are asking to access to user's data while they are not signed in
                .append("&approval_prompt=force"); // this requires them to verify which account to use, if they are already signed in
        return oauthUrl.toString();
    }

    //    https://stackoverflow.com/questions/19766912/how-do-i-authorise-an-app-web-or-installed-without-user-intervention
//https://stackoverflow.com/questions/28437149/how-to-do-oauth2-to-google-with-servlets-simple-way-to-do-sign-in-with-googl

    // Mailman
    // Client ID = 191982532326-rns5qtpuqpfaa6kg0trmtg0f2ugh0k97.apps.googleusercontent.com
    // Client Secret = LUuJG2qn5CppMS1ACumXeDMV

//https://localhost:4200/admin/mail?state=GMAILCONNECT&code=4/sQGGvmJIoPqOE8W-B4bNmIo-wyMSGJWFjC_ZREYEW3leQ6Ak3rAlQsMCIJQkeNw2Ux6XhA2pGZutOawqBEFFXQE&scope=https://www.googleapis.com/auth/gmail.send
//    http://localhost:4200/admin/mail
}
