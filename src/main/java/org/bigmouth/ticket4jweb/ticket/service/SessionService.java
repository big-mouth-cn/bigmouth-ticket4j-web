package org.bigmouth.ticket4jweb.ticket.service;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bigmouth.framework.util.BaseLifeCycleSupport;
import org.bigmouth.ticket4j.PassCode;
import org.bigmouth.ticket4j.User;
import org.bigmouth.ticket4j.entity.Passenger;
import org.bigmouth.ticket4j.entity.response.CheckPassCodeResponse;
import org.bigmouth.ticket4j.entity.response.LoginSuggestResponse;
import org.bigmouth.ticket4j.entity.response.QueryPassengerResponse;
import org.bigmouth.ticket4j.http.Ticket4jHttpResponse;
import org.bigmouth.ticket4jweb.ticket.dao.SessionDao;
import org.bigmouth.ticket4jweb.ticket.entity.Session;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;


public class SessionService extends BaseLifeCycleSupport {
    
    private static final Map<String, Session> SESSIONS = Maps.newConcurrentMap();
    private SessionDao sessionDao;
    
    private User user;
    private PassCode passCode;

    public Session login(String username, String passwd, String passCode, Ticket4jHttpResponse ticket4jHttpResponse) {
        CheckPassCodeResponse checkLoginResp = this.passCode.checkLogin(ticket4jHttpResponse, passCode);
        Preconditions.checkArgument(checkLoginResp.isContinue(), "验证码不正确");
        LoginSuggestResponse response = user.login(username, passwd, passCode, ticket4jHttpResponse);
        Preconditions.checkNotNull(response, "登陆失败，原因暂时不明，请确认12306官方是否正常");
        Preconditions.checkArgument(response.isContinue(), response.getMessage());
        QueryPassengerResponse passenger = user.queryPassenger(ticket4jHttpResponse);
        
        Session session = new Session();
        session.setUsername(username);
        session.setPassword(passwd);
        session.setResponse(response);
        session.setTicket4jHttpResponse(ticket4jHttpResponse);
        if (null != passenger)
            session.setPassengers(passenger.getData().getDatas());
        session.setSignIn(true);
        put(username, session);
        return session;
    }
    
    public List<Passenger> getPassengers(Ticket4jHttpResponse ticket4jHttpResponse) {
        QueryPassengerResponse passenger = user.queryPassenger(ticket4jHttpResponse);
        if (null != passenger) {
            return passenger.getData().getDatas();
        }
        return Lists.newArrayList();
    }
    
    public void put(String username, Session session) {
        sessionDao.insert(session);
        SESSIONS.put(username, session);
    }
    
    public List<Session> get() {
        List<Session> list = Lists.newArrayList();
        for (Entry<String, Session> session : SESSIONS.entrySet()) {
            list.add(session.getValue());
        }
        return list;
    }
    
    @Override
    protected void doInit() {
        List<Session> sessions = sessionDao.queryAll();
        for (Session session : sessions) {
            SESSIONS.put(session.getUsername(), session);
        }
    }

    @Override
    protected void doDestroy() {
        SESSIONS.clear();
    }

    public Session get(String username) {
        return SESSIONS.get(username);
    }
    
    public void setUser(User user) {
        this.user = user;
    }

    public void setPassCode(PassCode passCode) {
        this.passCode = passCode;
    }

    public void setSessionDao(SessionDao sessionDao) {
        this.sessionDao = sessionDao;
    }
}
