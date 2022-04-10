package ch.uzh.ifi.hase.soprafs22.rest.dto;

public class UserGetTokenDTO {


    private Long id;
    private String username;
    private String token;

    public String getToken() {return token;}
    public void setToken(String token) {this.token = token;}
    public Long getId() {return id;}

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
