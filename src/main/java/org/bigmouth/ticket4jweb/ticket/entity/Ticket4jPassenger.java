package org.bigmouth.ticket4jweb.ticket.entity;

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;


public class Ticket4jPassenger implements Serializable {

    private static final long serialVersionUID = 9184700363100371484L;

    private String name;
    private String value;
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getValue() {
        return value;
    }
    
    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
