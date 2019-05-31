package csl.notification;


import csl.database.model.UserAccount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;

public class MailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MailService.class);

    public static void sendPasswordRetrievalMail(String email, String unhashedTemporaryPassword, UserAccount account) {
        Session secureSession = getSession();

        try {
            // Create a default MimeMessage object.
            MimeMessage message = new MimeMessage(secureSession);
            message.setFrom(new InternetAddress("macrologwebapp@gmail.com"));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(email));
            message.setSubject("Macrolog Credentials");
            message.setContent("<h3>Hello " + account.getUsername() + ", </h3>" +
                            "<p>A request has been made to reset your password. " +
                            "We have generated a new password for you: <i>" + unhashedTemporaryPassword + "</i>. " +
                            "You can use this within 30 minutes to log in and choose a new password of your own." +
                            "If you did not request this password change, you can ignore this messsage.</p>" +
                            "<p>See you soon!</p>" +
                            "<p>Carmen and Arjan from Macrolog</p>"
                    , "text/html");

            Transport.send(message);
        } catch (MessagingException ex) {
            LOGGER.error(ex.getMessage());
        }
    }

    public static void sendConfirmationMail(String email, UserAccount account) {
        Session secureSession = getSession();

        try {
            // Create a default MimeMessage object.
            MimeMessage message = new MimeMessage(secureSession);
            message.setFrom(new InternetAddress("macrologwebapp@gmail.com"));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(email));
            message.setSubject("Welcome to Macrolog!");
            message.setContent("<p>Hello " + account.getUsername() + ", </p>" +
                            "<p>Thank you for using Macrolog!</p>" +
                            "<p>This app started out as a hobby project. " +
                            "It was developed by two software engineers who wanted to get in shape, " +
                            "who also happen to hate adds, and who (probably incorrectly) " +
                            "think they could do a better job on this " +
                            "than the multitude of other food-tracking-app developers. </p>" + "" +
                            "<p>Our aim is to make it as easy as possible to log your food intake on a daily basis. " +
                            "We hope this app ultimately helps you to achieve your goals, whatever they may be. </p>" +
                            "<p>All the best,</p>" +
                            "<p>Carmen and Arjan from Macrolog</p>"
                    , "text/html");

            Transport.send(message);
        } catch (MessagingException ex) {
            LOGGER.error(ex.getMessage());
        }
    }


    private static Session getSession() {
        String username = "macrologwebapp@gmail.com";
        String password = "Macrolog01";

        Properties properties = System.getProperties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");

        // Get the default Session object.
        return Session.getInstance(properties,
                new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                }
        );
    }

}
