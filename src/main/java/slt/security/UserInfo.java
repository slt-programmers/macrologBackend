package slt.security;

public class UserInfo {

    private Integer userId;

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getUserId() {
        return userId;
    }

    @Override
    public String toString() {
        return "userId = " + userId;
    }
}