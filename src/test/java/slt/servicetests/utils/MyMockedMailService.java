package slt.servicetests.utils;

import slt.config.MailConfig;
import slt.database.entities.UserAccount;
import slt.notification.MailService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MyMockedMailService extends MailService {

    private HashMap<String,String> resetPasswords = new HashMap<>();
    private List<String> confirmationMailsSend = new ArrayList<>();

    public MyMockedMailService(MailConfig mailConfig) {
        super(mailConfig);
    }

    @Override
    public void sendPasswordRetrievalMail(String email, String unhashedTemporaryPassword, UserAccount account) {
        resetPasswords.put(email, unhashedTemporaryPassword);
    }

    public String getResettedPassword(String email) {
        return resetPasswords.get(email);
    }

    @Override
    public void sendConfirmationMail(String email, UserAccount account) {
        confirmationMailsSend.add(email);
    }
    public boolean verifyConfirmationSendTo(String email){
        return confirmationMailsSend.remove(email);
    }
}
