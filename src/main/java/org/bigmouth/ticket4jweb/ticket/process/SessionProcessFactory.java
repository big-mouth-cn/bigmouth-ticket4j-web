package org.bigmouth.ticket4jweb.ticket.process;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.bigmouth.framework.util.BaseLifeCycleSupport;
import org.bigmouth.framework.web_socket.WebSocketFactory;
import org.bigmouth.ticket4j.User;
import org.bigmouth.ticket4j.entity.response.CheckUserResponse;
import org.bigmouth.ticket4j.http.Ticket4jHttpResponse;
import org.bigmouth.ticket4jweb.ticket.entity.Session;
import org.bigmouth.ticket4jweb.ticket.entity.WebSocketMessage;
import org.bigmouth.ticket4jweb.ticket.entity.WebSocketMessageType;
import org.bigmouth.ticket4jweb.ticket.service.SessionService;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;


public class SessionProcessFactory extends BaseLifeCycleSupport {

    public static final long SLEEP_TIME = 10 * 60 * 1000; // 10 minutes.
    
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    private final SessionService sessionService;
    private final User user;
    
    private WebSocketFactory webSocketFactory;
    
    public SessionProcessFactory(SessionService sessionService, User user) {
        Preconditions.checkNotNull(sessionService, "sessionService");
        Preconditions.checkNotNull(user, "user");
        this.sessionService = sessionService;
        this.user = user;
    }
    
    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        }
        catch (InterruptedException e) {
        }
    }
    
    @Override
    protected void doInit() {
        EXECUTOR.submit(new Runnable() {
            
            @Override
            public void run() {
                while (true) {
                    List<Session> sessions = sessionService.get();
                    for (Session session : sessions) {
                        Ticket4jHttpResponse ticket4jHttpResponse = session.getTicket4jHttpResponse();
                        CheckUserResponse response = user.check(ticket4jHttpResponse);
                        session.setSignIn(response.isContinue());
                        sessionService.put(session.getUsername(), session);
                        
                        WebSocketMessage<Session> message = new WebSocketMessage<Session>(WebSocketMessageType.SESSION_EVENT, session);
                        String text = new Gson().toJson(message);
                        webSocketFactory.getWebSocket().print(text);
                    }
                    sleep(SLEEP_TIME);
                }
            }
        });
    }

    @Override
    protected void doDestroy() {
        EXECUTOR.shutdownNow();
    }

    public void setWebSocketFactory(WebSocketFactory webSocketFactory) {
        this.webSocketFactory = webSocketFactory;
    }
}
