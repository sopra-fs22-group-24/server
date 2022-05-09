package ch.uzh.ifi.hase.soprafs22.rest.dto;

public class ScoreDTO {
    private String username;
    private int score;

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
