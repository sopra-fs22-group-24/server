package ch.uzh.ifi.hase.soprafs22.rest.dto;

public class NCardsDTO {

    private String username;
    private Integer nCards;

    public NCardsDTO() {}
    public Integer getnCards() {
        return nCards;
    }

    public String getUsername() {
        return username;
    }

    public void setnCards(Integer nCards) {
        this.nCards = nCards;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
