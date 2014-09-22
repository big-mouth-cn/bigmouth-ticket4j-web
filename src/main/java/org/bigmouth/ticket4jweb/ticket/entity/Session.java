package org.bigmouth.ticket4jweb.ticket.entity;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.bigmouth.ticket4j.entity.Passenger;
import org.bigmouth.ticket4j.entity.response.LoginSuggestResponse;
import org.bigmouth.ticket4j.http.Ticket4jHttpResponse;

public class Session implements Serializable {

    private static final long serialVersionUID = 185350595451299232L;

    private String username;
    private String password;
    private LoginSuggestResponse response;
    private Ticket4jHttpResponse ticket4jHttpResponse;
    private List<Passenger> passengers;
    private boolean signIn = false;
    
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }

    public LoginSuggestResponse getResponse() {
        return response;
    }

    public void setResponse(LoginSuggestResponse response) {
        this.response = response;
    }

    public Ticket4jHttpResponse getTicket4jHttpResponse() {
        return ticket4jHttpResponse;
    }

    public void setTicket4jHttpResponse(Ticket4jHttpResponse ticket4jHttpResponse) {
        this.ticket4jHttpResponse = ticket4jHttpResponse;
    }

    public List<Passenger> getPassengers() {
        return passengers;
    }

    public void setPassengers(List<Passenger> passengers) {
        this.passengers = passengers;
    }
    
    public boolean isSignIn() {
        return signIn;
    }
    
    public void setSignIn(boolean signIn) {
        this.signIn = signIn;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
