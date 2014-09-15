package org.bigmouth.ticket4jweb.ticket.entity;

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;


public class Ticket4jOrder implements Serializable {

    private static final long serialVersionUID = -69363851175636384L;

    private String id;
    /** 它可能是过期的 */
    private Session session;
    private String trainDate;
    private String startStation;
    private String endStation;
    private String[] includes;
    private String[] excludes;
    private Ticket4jSeat[] seatTypes;
    private Ticket4jPassenger[] passengers;
    
    public static final int NORMAL = 0;
    public static final int COMPLETED = 1;
    public static final int STOP = 2;
    public static final int REPEAT = 3;
    
    public static final int WAIT = 4;
    
    public static final int NULL = -1;
    public static final int SESSION_TIME_OUT = -2;
    public static final int NO_TICKET = -3;
    public static final int NO_COMPLETE_ORDER = -4;
    public static final int CANCEL_ORDER_MAX = -5;
    public static final int WAIT_TIME = -6;
    public static final int RUNTIME = -7;
    
    //
    private int statusCode;
    private String message;
    
    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }

    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }

    public Session getSession() {
        return session;
    }
    
    public void setSession(Session session) {
        this.session = session;
    }
    
    public String getTrainDate() {
        return trainDate;
    }
    
    public void setTrainDate(String trainDate) {
        this.trainDate = trainDate;
    }
    
    public String getStartStation() {
        return startStation;
    }
    
    public void setStartStation(String startStation) {
        this.startStation = startStation;
    }
    
    public String getEndStation() {
        return endStation;
    }
    
    public void setEndStation(String endStation) {
        this.endStation = endStation;
    }
    
    public String[] getIncludes() {
        return includes;
    }
    
    public void setIncludes(String[] includes) {
        this.includes = includes;
    }
    
    public String[] getExcludes() {
        return excludes;
    }
    
    public void setExcludes(String[] excludes) {
        this.excludes = excludes;
    }
    
    public Ticket4jSeat[] getSeatTypes() {
        return seatTypes;
    }

    public void setSeatTypes(Ticket4jSeat[] seatTypes) {
        this.seatTypes = seatTypes;
    }
    
    public Ticket4jPassenger[] getPassengers() {
        return passengers;
    }

    public void setPassengers(Ticket4jPassenger[] passengers) {
        this.passengers = passengers;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
