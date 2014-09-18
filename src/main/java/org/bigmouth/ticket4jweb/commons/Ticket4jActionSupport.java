package org.bigmouth.ticket4jweb.commons;

import java.io.IOException;
import java.io.PrintWriter;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.bigmouth.framework.util.DateUtils;
import org.bigmouth.framework.web.action.json.JsonActionSupport;
import org.bigmouth.ticket4j.entity.Response;
import org.bigmouth.ticket4j.http.Ticket4jHttpResponse;
import org.bigmouth.ticket4jweb.ticket.entity.Session;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


public class Ticket4jActionSupport extends JsonActionSupport {
    
    private static final long serialVersionUID = -1560801099509933609L;
    
    protected Session getSession() {
        String parameter = getParameter("session");
        Preconditions.checkArgument(StringUtils.isNotBlank(parameter), "获取Session失败");
        return fromJson(parameter, Session.class);
    }
    
    protected Ticket4jHttpResponse getTicket4jHttpResponse() {
        String parameter = getParameter("ticket4jHttpResponse");
        Preconditions.checkArgument(StringUtils.isNotBlank(parameter), "没有初始化成功");
        Ticket4jHttpResponse ticket4jHttpResponse = fromJson(parameter, Ticket4jHttpResponse.class);
        return ticket4jHttpResponse;
    }

    protected void doResponse(Object respData) throws IOException {
        PrintWriter pw = null;
        try {
            pw = getResponse().getWriter();
            Gson gson = new GsonBuilder()
                .setDateFormat(DateUtils.LONG_DATE_FORMAT)
                .create();
            pw.write(gson.toJson(respData));
            pw.flush();
        }
        finally {
            IOUtils.closeQuietly(pw);
        }
    }
    
    protected void doResponse(Response response) {
        Preconditions.checkNotNull(response, "响应结果为空");
        int statusCode = response.isContinue() ? 0 : -1; 
        doResponseObject(statusCode, response.getMessage(), response);
    }
    
    public <T> T fromJson(String json, Class<T> cls) {
        return new Gson().fromJson(json, cls);
    }
}
