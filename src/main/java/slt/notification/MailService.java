package slt.notification;


import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import slt.config.MailConfig;
import slt.database.entities.UserAccount;

import javax.mail.*;
import javax.mail.internet.MimeMessage;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Service
@Slf4j
@AutoConfigureBefore(MailConfig.class)
@EnableConfigurationProperties(MailConfig.class)
public class MailService {

    private MailConfig mailConfig;

    Consumer<Message> consumer = message -> {
        try{
            Transport.send(message);
        } catch (MessagingException ex){
            log.error("Error sending mail",ex);
        }
    };

    Supplier<Message> supplier = () -> {
        Properties properties = getSessionProperties();
        String username = mailConfig.getUsername();
        String password = mailConfig.getPassword();
        Session instance = Session.getInstance(properties,
                new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });
        return new MimeMessage(instance);
    };

    SendEmailFactory sendEmailFactory;

    public MailService(MailConfig mailConfig) {
        this.mailConfig = mailConfig;
        sendEmailFactory = new SendEmailFactory(supplier, consumer);
    }


    public void sendPasswordRetrievalMail(String email, String unhashedTemporaryPassword, UserAccount account) {

        try{


            String from = "macrologwebapp@gmail.com";
            String subject = "Macrolog Credentials";
            String body = "<h3>Hello " + account.getUsername() + ", </h3>" +
                            "<p>A request has been made to reset your password. </p>" +
                            "<p>We have generated a new password for you: <i>" + unhashedTemporaryPassword + "</i>. </p>" +
                            "<p>You can use this within 30 minutes to log in and choose a new password of your own. </p>" +
                            "<p>If you did not request this password change, you can ignore this messsage. </p>" +
                            "<p>See you soon! </p>" +
                            "<p>Carmen and Arjan from Macrolog </p>";

            log.debug("Mail send to"  + email);
            sendEmailFactory.send(email, from, subject, body);
        } catch (MessagingException ex) {
            log.error(ex.getMessage());
        }
    }

    public void sendConfirmationMail(String email, UserAccount account) {
        try {

            String from = "macrologwebapp@gmail.com";
            String subject = "Welcome to Macrolog!";
            String body = "<p>Hello " + account.getUsername() + ", </p>" +
                            "<p>Thank you for using Macrolog!</p>" +
                            "<p>You are now ready to use both the app and the <a href=\"https://macrolog.herokuapp.com/\"> website</a>. " +
                            "Our aim is to make it as easy as possible to log your food intake on a daily basis. " +
                            "We hope this app ultimately helps you to achieve your goals, whatever they may be. </p>" +
                            "<p>All the best,</p>" +
                            "<p>Carmen and Arjan from Macrolog</p>";


            log.debug("Mail send to"  + email);

            sendEmailFactory.send(email, from, subject, body);

        } catch (MessagingException ex) {
            log.error(ex.getMessage());
        }
    }

    private Properties getSessionProperties() {
        Properties properties = System.getProperties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", mailConfig.getHost());
        properties.put("mail.smtp.port", mailConfig.getPort());
       return properties;
    }

}

