package slt.notification;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class SendEmailFactory {
    private final Supplier<Message> messageSupplier;
    private final Consumer<Message> messageSender;

    public SendEmailFactory(Supplier<Message> messageSupplier,
                            Consumer<Message> messageSender) {
        this.messageSupplier = messageSupplier;
        this.messageSender = messageSender;
    }

    public void send(String address, String from,
                     String subject, String body)
            throws MessagingException {
        Message message = messageSupplier.get();

        message.addRecipient
                (Message.RecipientType.TO, new InternetAddress(address));
        message.addFrom(new InternetAddress[]{new InternetAddress(from)});
        message.setSubject(subject);
        message.setContent(body, "text/html");
        messageSender.accept(message);
    }
}
