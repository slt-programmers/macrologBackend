package slt.notification;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import slt.config.MailConfig;
import slt.database.entities.UserAccount;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class MailServiceTest {

    @Test
    void sendConfirmationMail() throws MessagingException {

        MailConfig mailconfig = new MailConfig();
        mailconfig.setHost("host");
        mailconfig.setPort("1231");
        MailService ms = new MailService(mailconfig);

        ms.sendEmailFactory = Mockito.mock(SendEmailFactory.class);

        ms.sendConfirmationMail("to", UserAccount.builder().username("username").build());

        Mockito.verify(ms.sendEmailFactory).send(eq("to"),eq("macrologwebapp@gmail.com"), eq("Welcome to Macrolog!"), any());
    }

    @Test
    void sendPasswordRetrievalMail() throws MessagingException {

        MailConfig mailconfig = new MailConfig();
        mailconfig.setHost("host");
        mailconfig.setPort("1231");
        MailService ms = new MailService(mailconfig);

        ms.sendEmailFactory = Mockito.mock(SendEmailFactory.class);

        ms.sendPasswordRetrievalMail("to","password",UserAccount.builder().username("username").build());

        Mockito.verify(ms.sendEmailFactory).send(eq("to"),eq("macrologwebapp@gmail.com"), eq("Macrolog Credentials"), any());
    }

    @Test
    public void sendBasicEmail() throws MessagingException {
        final boolean[] messageCalled = {false};

        Consumer<Message> consumer = message -> {
            messageCalled[0] = true;
        };

        Message message = mock(Message.class);
        Supplier<Message> supplier = () -> message;

        SendEmailFactory sendMailFactory = new SendEmailFactory(supplier, consumer);

        String adress = "to@from.nl";
        String from = "from@fefa.nl";
        String subject = "Test Email";
        String body = "This is a sample email!";

        sendMailFactory.send(adress, from, subject, body);
        verify(message).addRecipient(Message.RecipientType.TO, new InternetAddress("to@from.nl"));
        verify(message).addFrom(new InternetAddress[]{new InternetAddress("from@fefa.nl")});
        verify(message).setSubject(subject);
        verify(message).setContent(body, "text/html");

        assertThat(messageCalled[0]).isTrue();
    }
}