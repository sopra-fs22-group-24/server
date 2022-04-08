package ch.uzh.ifi.hase.soprafs22.rest.dto;

public class UserGetTokenDTO extends UserGetDTO{
    private String token;
    public String getToken() {return token;}
    public void setToken(String token) {this.token = token;}
}
