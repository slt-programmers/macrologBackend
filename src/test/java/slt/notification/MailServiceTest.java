//package slt.notification;
//
//import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
//import com.google.api.client.http.HttpTransport;
//import com.google.api.client.util.Base64;
//import org.junit.jupiter.api.Test;
//import org.mockito.Mockito;
//import slt.config.MailConfig;
//import slt.database.entities.UserAccount;
//
//import javax.mail.Message;
//import javax.mail.MessagingException;
//import javax.mail.Session;
//import javax.mail.internet.InternetAddress;
//import javax.mail.internet.MimeMessage;
//import java.io.*;
//import java.util.Properties;
//import java.util.function.Consumer;
//import java.util.function.Supplier;
//
//import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.verify;
//import com.google.api.client.auth.oauth2.Credential;
//import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
//import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
//import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
//import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
//import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
//import com.google.api.client.http.javanet.NetHttpTransport;
//import com.google.api.client.json.JsonFactory;
//import com.google.api.client.json.jackson2.JacksonFactory;
//import com.google.api.client.util.store.FileDataStoreFactory;
//import com.google.api.services.gmail.Gmail;
//import com.google.api.services.gmail.GmailScopes;
//
//import java.security.GeneralSecurityException;
//
//class MailServiceTest {
//
//    private static final String APPLICATION_NAME = "macrologwebserver";
//    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
//    private static final String TOKENS_DIRECTORY_PATH = "tokens";
//
//    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";
//
//    public static com.google.api.services.gmail.model.Message createMessageWithEmail(MimeMessage emailContent)
//            throws MessagingException, IOException {
//        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
//        emailContent.writeTo(buffer);
//        byte[] bytes = buffer.toByteArray();
//        String encodedEmail = Base64.encodeBase64URLSafeString(bytes);
//        com.google.api.services.gmail.model.Message message = new com.google.api.services.gmail.model.Message();
//        message.setRaw(encodedEmail);
//        return message;
//    }
//
//    /**
//     * Creates an authorized Credential object.
//     * @param HTTP_TRANSPORT The network HTTP Transport.
//     * @return An authorized Credential object.
//     * @throws IOException If the credentials.json file cannot be found.
//     */
//    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
//        // Load client secrets.
//        InputStream in = MailServiceTest.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
//        if (in == null) {
//            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
//        }
//        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
//
//        // Build flow and trigger user authorization request.
//        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
//                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, GmailScopes.all())
//                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
//                .setAccessType("offline")
//                .build();
//        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
//        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
//    }
//
//    public static MimeMessage createEmail(String to,
//                                          String from,
//                                          String subject,
//                                          String bodyText)
//            throws MessagingException {
//        Properties props = new Properties();
//        Session session = Session.getDefaultInstance(props, null);
//
//        MimeMessage email = new MimeMessage(session);
//
//        email.setFrom(new InternetAddress(from));
//        email.addRecipient(javax.mail.Message.RecipientType.TO,
//                new InternetAddress(to));
//        email.setSubject(subject);
//        email.setText(bodyText);
//        return email;
//    }
//
//    public static void main(String... args) throws IOException, GeneralSecurityException {
//        // Build a new authorized API client service.
//
//        String jsonFile = "/macrolog-d35f03915a46.json";
//        InputStream in = MailServiceTest.class.getResourceAsStream(jsonFile);
//        if (in == null) {
//            throw new FileNotFoundException("Resource not found: " + jsonFile);
//        }
////        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
//
//
////       if (false) {
////           final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
////           Gmail service = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
////                   .setApplicationName(APPLICATION_NAME)
////                   .build();
////
////
////           // Print the labels in the user's account.
////           String user = "me";
////           com.google.api.services.gmail.model.Message message = null;
////           try {
////               message = createMessageWithEmail(createEmail("arjant.tienkamp@gmail.com", user, "test", "test"));
////           } catch (MessagingException e) {
////               e.printStackTrace();
////           }
////           final com.google.api.services.gmail.model.Message execute = service.users().messages().send("me", message).execute();
////           System.out.println("Message id: " + execute.getId());
////           System.out.println(execute.toPrettyString());
////       }
////
//        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
//        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
////        String serviceAccount = "macrologwebserver@macrolog.iam.gserviceaccount.com";
//        String serviceAccount = "macrologwebserver@macrolog.iam.gserviceaccount.com";
//        GoogleCredential credential = GoogleCredential.fromStream(in)
//                .createScoped(GmailScopes.all());
//
//        GoogleCredential.Builder builder = new GoogleCredential.Builder()
//                .setTransport(httpTransport)
//                .setJsonFactory(jsonFactory)
//                .setServiceAccountProjectId("macrolog")
//                .setServiceAccountScopes(GmailScopes.all())
//                .setServiceAccountId(credential.getServiceAccountId())
//                .setServiceAccountPrivateKey(credential.getServiceAccountPrivateKey())
//                .setServiceAccountPrivateKeyId(credential.getServiceAccountPrivateKeyId())
////                .setTokenServerEncodedUrl(credential.getTokenServerEncodedUrl())
//                .setServiceAccountUser(serviceAccount);
//
//        final GoogleCredential delegated = builder.build().createDelegated("macrologwebapp@gmail.com");
//        delegated.refreshToken();
//        final Gmail service = new Gmail.Builder(httpTransport, jsonFactory, delegated).setApplicationName("macrolog").build();
//
////         String user = "me";
//           com.google.api.services.gmail.model.Message message = null;
//           try {
//               message = createMessageWithEmail(createEmail("arjan.tienkamp@gmail.com", serviceAccount, "test", "test"));
//           } catch (MessagingException e) {
//               e.printStackTrace();
//           }
//           final com.google.api.services.gmail.model.Message execute = service.users().messages().send("me",message).execute();
////      final ListMessagesResponse me = service.users().messages().list("macrologwebapp@gmail.com").execute();
////     System.out.println("Message id: " + me.getMessages().size());
//           System.out.println(execute.toPrettyString());
//
//
//        //        Gmail service = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).setApplicationName("macrologwebserver").build();
//
////        Credential credential2 = new GoogleCredential.Builder().setTransport(HTTP_TRANSPORT)
////                .setJsonFactory(JSON_FACTORY)
////                .setServiceAccountPrivateKeyFromPemFile()
//////                .setServiceAccountId(serviceAccount)
//////                .setClientSecrets(clientSecrets)
//////                .setServiceAccount
////                .setServiceAccountScopes(Collections.singleton(GmailScopes.GMAIL_SEND))
////                .build();
////        Gmail service = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).setApplicationName("macrologwebserver").build();
////        String user = "me"; //Tried direct email id also
////        service.users().labels().list(user).execute();
//    }
//
//
//    @Test
//    void sendConfirmationMail() throws MessagingException {
//
//        MailConfig mailconfig = new MailConfig();
//        mailconfig.setHost("host");
//        mailconfig.setPort("1231");
//        MailService ms = new MailService(mailconfig);
//
//        ms.sendEmailFactory = Mockito.mock(SendEmailFactory.class);
//
//        ms.sendConfirmationMail("to", UserAccount.builder().username("username").build());
//
//        Mockito.verify(ms.sendEmailFactory).send(eq("to"),eq("macrologwebapp@gmail.com"), eq("Welcome to Macrolog!"), any());
//    }
//
//    @Test
//    void sendPasswordRetrievalMail() throws MessagingException {
//
//        MailConfig mailconfig = new MailConfig();
//        mailconfig.setHost("host");
//        mailconfig.setPort("1231");
//        MailService ms = new MailService(mailconfig);
//
//        ms.sendEmailFactory = Mockito.mock(SendEmailFactory.class);
//
//        ms.sendPasswordRetrievalMail("to","password",UserAccount.builder().username("username").build());
//
//        Mockito.verify(ms.sendEmailFactory).send(eq("to"),eq("macrologwebapp@gmail.com"), eq("Macrolog Credentials"), any());
//    }
//
//    @Test
//    public void sendBasicEmail() throws MessagingException {
//        final boolean[] messageCalled = {false};
//
//        Consumer<Message> consumer = message -> {
//            messageCalled[0] = true;
//        };
//
//        Message message = mock(Message.class);
//        Supplier<Message> supplier = () -> message;
//
//        SendEmailFactory sendMailFactory = new SendEmailFactory(supplier, consumer);
//
//        String adress = "to@from.nl";
//        String from = "from@fefa.nl";
//        String subject = "Test Email";
//        String body = "This is a sample email!";
//
//        sendMailFactory.send(adress, from, subject, body);
//        verify(message).addRecipient(Message.RecipientType.TO, new InternetAddress("to@from.nl"));
//        verify(message).addFrom(new InternetAddress[]{new InternetAddress("from@fefa.nl")});
//        verify(message).setSubject(subject);
//        verify(message).setContent(body, "text/html");
//
//        assertThat(messageCalled[0]).isTrue();
//    }
//
//
//}