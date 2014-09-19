package org.bigmouth.ticket4jweb.ticket.dao;

import java.util.List;

import org.bigmouth.ticket4jweb.ticket.entity.Ticket4jOrder;


public interface OrderDao {

    void insert(Ticket4jOrder ticket4jOrder);
    
    void delete(String id);
    
    List<Ticket4jOrder> queryAll();
}
