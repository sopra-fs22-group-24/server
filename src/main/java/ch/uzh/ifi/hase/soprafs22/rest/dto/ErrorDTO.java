package ch.uzh.ifi.hase.soprafs22.rest.dto;

public class ErrorDTO {
    private String error;
    private String msg;

    public void setError(String error) {
        this.error = error;
    }

    public String getError() {
        return error;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
