package org.bigmouth.ticket4jweb.ticket.dao;

import java.util.List;

import org.bigmouth.ticket4jweb.ticket.entity.Session;


public interface SessionDao {

    void insert(Session session);
    
    void delete(String username);
    
    List<Session> queryAll();
}
