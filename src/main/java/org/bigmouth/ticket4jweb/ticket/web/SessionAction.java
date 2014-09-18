package org.bigmouth.ticket4jweb.ticket.web;

import java.io.File;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.bigmouth.ticket4j.Initialize;
import org.bigmouth.ticket4j.PassCode;
import org.bigmouth.ticket4j.entity.Passenger;
import org.bigmouth.ticket4j.http.Ticket4jHttpResponse;
import org.bigmouth.ticket4j.utils.AntiUtils;
import org.bigmouth.ticket4jweb.commons.FileService;
import org.bigmouth.ticket4jweb.commons.Ticket4jActionSupport;
import org.bigmouth.ticket4jweb.ticket.entity.Session;
import org.bigmouth.ticket4jweb.ticket.service.SessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

@Namespace("/")
@Action("session")
public class SessionAction extends Ticket4jActionSupport {

    private static final long serialVersionUID = -218874523734678808L;
    private static final Logger LOGGER = LoggerFactory.getLogger(SessionAction.class);

    private SessionService sessionService;
    private FileService fileService;
    
    private Initialize initialize;
    private PassCode passCode;
    private AntiUtils anti;

    /**
     * 初始化，获取Cookie
     */
    public void initialize() {
        Ticket4jHttpResponse response = initialize.init();
        if (null != response) {
            succeed(response);
        }
        else {
            failed();
        }
    }
    
    public void getLoginPassCode() {
        try {
            Ticket4jHttpResponse ticket4jHttpResponse = getTicket4jHttpResponse();
            File file = passCode.getLoginPassCode(ticket4jHttpResponse);
            String code = anti.recognition(file.getPath());
            String path = fileService.store(file, file.getName());
            succeed(new String[] { "rec", "path" }, new String[] { code, path });
        }
        catch (Exception e) {
            failed(e.getMessage());
            LOGGER.error("getLoginPassCode:", e);
        }
    }

    public void login() {
        try {
            String username = getParameter("username");
            String passwd = getParameter("passwd");
            String passCode = getParameter("loginPassCode");
            Preconditions.checkArgument(StringUtils.isNotBlank(username), "登录名不能为空");
            Preconditions.checkArgument(StringUtils.isNotBlank(passwd), "密码不能为空");
            Preconditions.checkArgument(StringUtils.isNotBlank(passCode), "登陆验证码不能为空");
            Ticket4jHttpResponse ticket4jHttpResponse = getTicket4jHttpResponse();
            Session session = sessionService.login(username, passwd, passCode, ticket4jHttpResponse);
            succeed(session);
        }
        catch (Exception e) {
            failed(e.getMessage());
            LOGGER.error("login:", e);
        }
    }
    
    public void getSessions() {
        try {
            List<Session> list = sessionService.get();
            succeed(list);
        }
        catch (Exception e) {
            failed(e.getMessage());
            LOGGER.error("getSessions:", e);
        }
    }
    
    public void getPassengers() {
        try {
            Session session = getSession();
            Ticket4jHttpResponse ticket4jHttpResponse = session.getTicket4jHttpResponse();
            List<Passenger> passengers = sessionService.getPassengers(ticket4jHttpResponse);
            if (CollectionUtils.isNotEmpty(passengers))
                succeed(passengers);
            else 
                failed("没有常用联系人，有可能加载失败，请重试。");
        }
        catch (Exception e) {
            failed(e.getMessage());
            LOGGER.error("getPassengers:", e);
        }
    }

    public void setInitialize(Initialize initialize) {
        this.initialize = initialize;
    }

    public void setPassCode(PassCode passCode) {
        this.passCode = passCode;
    }

    public void setFileService(FileService fileService) {
        this.fileService = fileService;
    }

    public void setAnti(AntiUtils anti) {
        this.anti = anti;
    }

    public void setSessionService(SessionService sessionService) {
        this.sessionService = sessionService;
    }
}
