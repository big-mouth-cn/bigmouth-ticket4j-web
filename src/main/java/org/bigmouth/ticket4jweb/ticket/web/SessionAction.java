package org.bigmouth.ticket4jweb.ticket.web;

import java.io.File;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.bigmouth.ticket4j.Initialize;
import org.bigmouth.ticket4j.PassCode;
import org.bigmouth.ticket4j.User;
import org.bigmouth.ticket4j.entity.Response;
import org.bigmouth.ticket4j.http.Ticket4jHttpResponse;
import org.bigmouth.ticket4jweb.commons.FileService;
import org.bigmouth.ticket4jweb.commons.Ticket4jActionSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

@Namespace("/")
@Action("session")
public class SessionAction extends Ticket4jActionSupport {

    private static final long serialVersionUID = -218874523734678808L;
    private static final Logger LOGGER = LoggerFactory.getLogger(SessionAction.class);

    private FileService fileService;
    
    private Initialize initialize;
    private PassCode passCode;
    private User user;

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
            String path = fileService.store(file, file.getName());
            succeed(path);
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
            Response response = user.login(username, passwd, passCode, ticket4jHttpResponse);
            succeed(response);
        }
        catch (Exception e) {
            failed(e.getMessage());
            LOGGER.error("login:", e);
        }
    }

    private Ticket4jHttpResponse getTicket4jHttpResponse() {
        String parameter = getParameter("ticket4jHttpResponse");
        Preconditions.checkArgument(StringUtils.isNotBlank(parameter), "没有初始化成功");
        Ticket4jHttpResponse ticket4jHttpResponse = fromJson(parameter, Ticket4jHttpResponse.class);
        return ticket4jHttpResponse;
    }

    public void setInitialize(Initialize initialize) {
        this.initialize = initialize;
    }

    public void setPassCode(PassCode passCode) {
        this.passCode = passCode;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setFileService(FileService fileService) {
        this.fileService = fileService;
    }
}
