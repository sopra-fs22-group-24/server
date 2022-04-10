package ch.uzh.ifi.hase.soprafs22.rest.dto;

public class UserPostTokenDTO {
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    String token;
}
