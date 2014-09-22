package org.bigmouth.ticket4jweb.ticket.service;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.bigmouth.framework.core.ResponseData;
import org.bigmouth.framework.util.PathUtils;
import org.bigmouth.ticket4j.Ticket4jDefaults;
import org.bigmouth.ticket4j.entity.order.OrderInfo;
import org.bigmouth.ticket4j.report.Report;
import org.bigmouth.ticket4j.utils.HttpClientUtils;
import org.bigmouth.ticket4j.utils.Ticket4jOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.gson.Gson;

public class NotificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationService.class);
    private static final String DIR = Ticket4jDefaults.PATH_TICKET4J;
    private static final String FILE = PathUtils.appendEndFileSeparator(DIR) + "RECEIVCER";
    private String sendUrl;
    
    public String read() throws IOException, ClassNotFoundException {
        return Ticket4jOutputStream.read(new File(FILE), false);
    }
    
    public void save(String address) throws IOException {
        Ticket4jOutputStream.write(address, new File(FILE));
    }
    
    public void notification(Report report) {
        if (null == report) {
            LOGGER.error("report has be null.");
            return;
        }
        try {
            String to = read();
            StringBuilder content = new StringBuilder();
            List<OrderInfo> orders = report.getOrders();
            int count = 0;
            if (CollectionUtils.isNotEmpty(orders)) {
                count = orders.get(0).getTickets().size();
            }
            content.append("Hi，").append("您通过东皇钟刚刚 ");
            if (count > 0) {
                content.append("成功预订了 ").append(count).append(" 张车票，");
            }
            else {
                content.append("成功预订到了车票，");
            }
            content.append("请立即使用账号(<b>").append(report.getUsername()).append("</b>)登录12306客运服务后台进行支付！<b>");
            send(to, content.toString());
        }
        catch (Exception e) {
            LOGGER.error("notification:", e);
        }
    }

    public void send(String to, String content) throws ClientProtocolException, IOException {
        if (StringUtils.isBlank(to))
            return;
        if (StringUtils.isBlank(content))
            return;
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(sendUrl);

        try {
            addPair(httpPost, new NameValuePair[] {
                    new BasicNameValuePair("to", to),
                    new BasicNameValuePair("content", content)
            });
            HttpResponse httpResponse = httpClient.execute(httpPost);
            String responseBody = HttpClientUtils.getResponseBody(httpResponse);
            ResponseData response = new Gson().fromJson(responseBody, ResponseData.class);
            if (response.getResultCode() != ResponseData.SUCCEED) {
                throw new RuntimeException(response.getMessage());
            }
        }
        finally {
            httpClient.getConnectionManager().shutdown();
        }
    }
    
    private void addPair(HttpEntityEnclosingRequestBase requestBase, NameValuePair... pairs) throws UnsupportedEncodingException {
        List<NameValuePair> list = Lists.newArrayList(pairs);
        requestBase.setEntity(new UrlEncodedFormEntity(list, Ticket4jDefaults.DEFAULT_CHARSET));
    }

    public void setSendUrl(String sendUrl) {
        this.sendUrl = sendUrl;
    }
}
