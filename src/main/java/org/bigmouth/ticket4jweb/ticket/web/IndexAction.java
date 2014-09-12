package org.bigmouth.ticket4jweb.ticket.web;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.bigmouth.framework.web.action.SimpleActionSupport;
import org.bigmouth.ticket4jweb.commons.Ticket4jActionSupport;

@Namespace("/")
@Action("index")
@Results({
    @Result(name = SimpleActionSupport.SUCCESS, location = "/WEB-INF/pages/index.jsp")
})
public class IndexAction extends Ticket4jActionSupport {

    private static final long serialVersionUID = -8420517145228528703L;

    @Override
    public String execute() throws Exception {
        return SUCCESS;
    }
}
