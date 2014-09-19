package org.bigmouth.ticket4jweb.ticket.service;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.bigmouth.framework.session.util.UUIDUtils;
import org.bigmouth.framework.util.BaseLifeCycleSupport;
import org.bigmouth.ticket4j.Order;
import org.bigmouth.ticket4j.entity.response.NoCompleteOrderResponse;
import org.bigmouth.ticket4j.http.Ticket4jHttpResponse;
import org.bigmouth.ticket4jweb.ticket.dao.OrderDao;
import org.bigmouth.ticket4jweb.ticket.entity.Ticket4jOrder;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class OrderService extends BaseLifeCycleSupport {

    private static final Map<String, Ticket4jOrder> ORDERS = Maps.newConcurrentMap();
    private OrderDao orderDao;
    private Order order;
    
    public NoCompleteOrderResponse noComplete(Ticket4jHttpResponse ticket4jHttpResponse) {
        return order.queryNoComplete(ticket4jHttpResponse);
    }
    
    public void remove(String id) {
        orderDao.delete(id);
        ORDERS.remove(id);
    }
    
    public void save(Ticket4jOrder order) {
        String id = order.getId();
        if (StringUtils.isBlank(id)) {
            id = UUIDUtils.getUUID();
            order.setId(id);
        }
        orderDao.insert(order);
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

    @Override
    protected void doInit() {
        fillOrderQueue();
    }

    @Override
    protected void doDestroy() {
        ORDERS.clear();
    }

    private void fillOrderQueue() {
        List<Ticket4jOrder> orders = orderDao.queryAll();
        for (Ticket4jOrder ticket4jOrder : orders) {
            ORDERS.put(ticket4jOrder.getId(), ticket4jOrder);
        }
    }

    public void setOrderDao(OrderDao orderDao) {
        this.orderDao = orderDao;
    }
}
