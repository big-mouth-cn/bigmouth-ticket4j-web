package org.bigmouth.ticket4jweb.ticket.web;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.bigmouth.ticket4jweb.commons.Ticket4jActionSupport;
import org.bigmouth.ticket4jweb.ticket.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

@Namespace("/")
@Action("notification")
public class NotificationAction extends Ticket4jActionSupport {

    private static final long serialVersionUID = -8336045457378579814L;
    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationAction.class);
    
    private NotificationService notificationService;
    
    @Override
    public String execute() {
        try {
            String address = notificationService.read();
            succeed(address);
        }
        catch (Exception e) {
            failed("execute:" + e.getMessage());
            LOGGER.error("execute:", e);
        }
        return NONE;
    }
    
    public void save() {
        try {
            String to = getParameter("to");
            Preconditions.checkArgument(StringUtils.isNotBlank(to), "接收者邮箱地址不能为空");
            notificationService.save(to);
            succeed();
        }
        catch (Exception e) {
            failed("保存失败!" + e.getMessage());
            LOGGER.error("save:", e);
        }
    }

    public void send() {
        try {
            String to = getParameter("to");
            String content = getParameter("content");
            Preconditions.checkArgument(StringUtils.isNotBlank(to), "接收者邮箱地址不能为空");
            Preconditions.checkArgument(StringUtils.isNotBlank(content), "发送的内容不能为空");
            notificationService.send(to, content);
            succeed();
        }
        catch (Exception e) {
            failed("Failed to send!" + e.getMessage());
            LOGGER.error("send:", e);
        }
    }

    public void setNotificationService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }
}
