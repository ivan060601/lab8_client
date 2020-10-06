package ClientStuff;

import java.io.Serializable;

public class Respond<T> implements Serializable {
    private String msg;
    private T secondParameter;

    public Respond(String msg) {
        this.msg = msg;
    }

    public Respond(){

    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getSecondParameter() {
        return secondParameter;
    }

    public void setSecondParameter(T secondParameter) {
        this.secondParameter = secondParameter;
    }
}
