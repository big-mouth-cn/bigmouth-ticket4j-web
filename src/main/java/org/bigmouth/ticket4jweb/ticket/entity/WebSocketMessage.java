package org.bigmouth.ticket4jweb.ticket.entity;

import java.io.Serializable;

public class WebSocketMessage<T> implements Serializable {

    private static final long serialVersionUID = -8131419281861657068L;

    private WebSocketMessageType messageType;
    private T data;

    public WebSocketMessage() {
        super();
    }

    public WebSocketMessage(WebSocketMessageType messageType, T data) {
        super();
        this.messageType = messageType;
        this.data = data;
    }

    public WebSocketMessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(WebSocketMessageType messageType) {
        this.messageType = messageType;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
