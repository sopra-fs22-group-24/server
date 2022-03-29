package ch.uzh.ifi.hase.soprafs22.rest.dto;

public class UserPostDTO {
  //Lea: password und name ausgetauscht
  private String password;

  private String username;
  //Lea: password und name ausgetauscht
  public String getPassword() {
    return password;
  }
  //Lea: password und name ausgetauscht
  public void setPassword(String password) {
    this.password = password;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }
}
