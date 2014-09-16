package org.bigmouth.ticket4jweb.ticket.web;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.bigmouth.ticket4j.entity.response.NoCompleteOrderResponse;
import org.bigmouth.ticket4j.http.Ticket4jHttpResponse;
import org.bigmouth.ticket4jweb.commons.Ticket4jActionSupport;
import org.bigmouth.ticket4jweb.ticket.entity.Ticket4jOrder;
import org.bigmouth.ticket4jweb.ticket.process.OrderProcessFactory;
import org.bigmouth.ticket4jweb.ticket.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

@Namespace("/")
@Action("order")
public class OrderAction extends Ticket4jActionSupport {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(OrderAction.class);
    private static final long serialVersionUID = 6423809364421615566L;
    
    private OrderService orderService;
    private OrderProcessFactory orderProcessFactory;
    
    public void create() {
        try {
            String order = getParameter("order");
            Preconditions.checkArgument(StringUtils.isNotBlank(order), "非法的请求，参数有误");
            Ticket4jOrder entity = fromJson(order, Ticket4jOrder.class);
            orderService.write(entity);
            orderProcessFactory.putPool(entity.getId());
            succeed(orderService.getOrder(entity.getId())); // Get new status.
        }
        catch (Exception e) {
            failed(e.getMessage());
            LOGGER.error("create:", e);
        }
    }
    
    public void start() {
        try {
            String id = getParameter("id");
            Preconditions.checkArgument(StringUtils.isNotBlank(id), "非法的请求，参数有误");
            Ticket4jOrder order = orderService.getOrder(id);
            Preconditions.checkNotNull(order, "订单无效，建议重新创建");
            orderProcessFactory.putPool(id);
            succeed();
        }
        catch (Exception e) {
            failed(e.getMessage());
            LOGGER.error("delete:", e);
        }
    }
    
    public void stop() {
        try {
            String id = getParameter("id");
            Preconditions.checkArgument(StringUtils.isNotBlank(id), "非法的请求，参数有误");
            orderProcessFactory.putStopQueue(id);
            succeed();
        }
        catch (Exception e) {
            failed(e.getMessage());
            LOGGER.error("delete:", e);
        }
    }
    
    public void remove() {
        try {
            String id = getParameter("id");
            Preconditions.checkArgument(StringUtils.isNotBlank(id), "非法的请求，参数有误");
            orderProcessFactory.putStopQueue(id);
            orderService.remove(id);
            succeed();
        }
        catch (Exception e) {
            failed(e.getMessage());
            LOGGER.error("delete:", e);
        }
    }
    
    public void list() {
        try {
            List<Ticket4jOrder> list = orderService.getOrders();
            succeed(list);
        }
        catch (Exception e) {
            failed(e.getMessage());
            LOGGER.error("list:", e);
        }
    }
    
    public void noComplete() {
        try {
            Ticket4jHttpResponse ticket4jHttpResponse = getTicket4jHttpResponse();
            NoCompleteOrderResponse queryNoComplete = orderService.noComplete(ticket4jHttpResponse);
            succeed(queryNoComplete);
        }
        catch (Exception e) {
            failed(e.getMessage());
            LOGGER.error("noComplete:", e);
        }
    }

    public void setOrderService(OrderService orderService) {
        this.orderService = orderService;
    }

    public void setOrderProcessFactory(OrderProcessFactory orderProcessFactory) {
        this.orderProcessFactory = orderProcessFactory;
    }
}
