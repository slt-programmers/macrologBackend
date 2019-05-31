package csl.database.model;

import java.time.LocalDateTime;

public class UserAccount {

    private long id;
    private String username;
    private String password;
    private String resetpassword;
    private LocalDateTime resetdate;
    private String email;

    public UserAccount(long id, String username, String password, String email, String resetpassword, LocalDateTime resetdate) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.resetdate=resetdate;
        this.resetpassword=resetpassword;
    }

    public long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getResetpassword() {
        return resetpassword;
    }

    public void setResetpassword(String resetpassword) {
        this.resetpassword = resetpassword;
    }

    public LocalDateTime getResetdate() {
        return resetdate;
    }

    public void setResetdate(LocalDateTime resetdate) {
        this.resetdate = resetdate;
    }
}
