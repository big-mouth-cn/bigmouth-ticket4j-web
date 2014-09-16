package org.bigmouth.ticket4jweb.ticket.process.thread;

import java.util.concurrent.Callable;

import org.bigmouth.ticket4j.Ticket;
import org.bigmouth.ticket4j.entity.request.QueryTicketRequest;
import org.bigmouth.ticket4j.entity.response.QueryTicketResponse;
import org.bigmouth.ticket4j.http.Ticket4jHttpResponse;


public class QueryTicketRunnable implements Callable<QueryTicketResponse> {

    private final Ticket ticket;
    private final Ticket4jHttpResponse ticket4jHttpResponse;
    private final QueryTicketRequest condition;
    
    public QueryTicketRunnable(Ticket ticket, Ticket4jHttpResponse ticket4jHttpResponse, QueryTicketRequest condition) {
        this.ticket = ticket;
        this.ticket4jHttpResponse = ticket4jHttpResponse;
        this.condition = condition;
    }

    @Override
    public QueryTicketResponse call() throws Exception {
        return ticket.query(ticket4jHttpResponse, condition);
    }
}
