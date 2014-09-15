package org.bigmouth.ticket4jweb.ticket.entity;

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.bigmouth.ticket4j.entity.Seat;

public class Ticket4jSeat implements Serializable {

    private static final long serialVersionUID = -3437060050473363361L;

    private String name;

    private Seat type;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Seat getType() {
        return type;
    }

    public void setType(Seat type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
