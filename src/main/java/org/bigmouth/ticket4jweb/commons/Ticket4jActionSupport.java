package org.bigmouth.ticket4jweb.commons;

import java.io.IOException;
import java.io.PrintWriter;

import org.apache.commons.io.IOUtils;
import org.bigmouth.framework.util.DateUtils;
import org.bigmouth.framework.web.action.json.JsonActionSupport;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


public class Ticket4jActionSupport extends JsonActionSupport {
    
    private static final long serialVersionUID = -1560801099509933609L;

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
    
    public <T> T fromJson(String json, Class<T> cls) {
        return new Gson().fromJson(json, cls);
    }
}
