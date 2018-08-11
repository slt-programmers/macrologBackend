package csl.notification;


import csl.database.model.UserAccount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;

public class MailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MailService.class);

    public static void sendMail(String email, UserAccount account) {
        String username = "marcologwebapp@gmail.com";
        String password = "macrolog";

        Properties properties = System.getProperties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");

        // Get the default Session object.
        Session secureSession = Session.getInstance(properties,
                new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                }
        );

        try {
            // Create a default MimeMessage object.
            MimeMessage message = new MimeMessage(secureSession);
            message.setFrom(new InternetAddress("macrologwebapp@gmail.com"));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(email));
            message.setSubject("Macrolog Webapp Credentials");
            message.setContent("<h3>Hello " + account.getUsername() + ", </h3>" +
                    "<p>Your password is "+ account.getPassword() +"</p>" +
                    "<p>See you back soon!</p>" +
                    "<p>Carmen and Arjan from Macrolog Webapp</p>"
                    , "text/html");

            Transport.send(message);
            LOGGER.info("Mail send to: " + email);
        } catch (MessagingException ex) {
            LOGGER.error(ex.getMessage());
        }

    }

}
