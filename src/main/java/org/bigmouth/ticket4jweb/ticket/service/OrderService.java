package org.bigmouth.ticket4jweb.ticket.service;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.bigmouth.framework.session.util.UUIDUtils;
import org.bigmouth.ticket4j.Order;
import org.bigmouth.ticket4j.entity.response.NoCompleteOrderResponse;
import org.bigmouth.ticket4j.http.Ticket4jHttpResponse;
import org.bigmouth.ticket4jweb.ticket.entity.Ticket4jOrder;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class OrderService {

    private static final Map<String, Ticket4jOrder> ORDERS = Maps.newConcurrentMap();
    private Order order;
    
    public NoCompleteOrderResponse noComplete(Ticket4jHttpResponse ticket4jHttpResponse) {
        return order.queryNoComplete(ticket4jHttpResponse);
    }
    
    public void remove(String id) {
        ORDERS.remove(id);
    }
    
    public void write(Ticket4jOrder order) {
        String id = order.getId();
        if (StringUtils.isBlank(id)) {
            id = UUIDUtils.getUUID();
            order.setId(id);
        }
        ORDERS.put(String.valueOf(id), order);
    }
    
    public List<Ticket4jOrder> getOrders() {
        List<Ticket4jOrder> orders = Lists.newArrayList();
        for (Entry<String, Ticket4jOrder> order : ORDERS.entrySet()) {
            orders.add(order.getValue());
        }
        return orders;
    }
    
    public Ticket4jOrder getOrder(String id) {
        return ORDERS.get(id);
    }

    public void setOrder(Order order) {
        this.order = order;
    }
}
