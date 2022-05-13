package ch.uzh.ifi.hase.soprafs22.rest.dto;

public class CreateLobbyDTO {
    private int maxSize;

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    public int getMaxSize() {
        return maxSize;
    }
}
