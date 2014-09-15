package org.bigmouth.ticket4jweb.ticket.service;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import org.bigmouth.ticket4jweb.ticket.entity.Ticket4jOrder;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class OrderService {

    private static final Map<String, Ticket4jOrder> ORDERS = Maps.newConcurrentMap();
    private final AtomicInteger count = new AtomicInteger(0);
    
    public void write(Ticket4jOrder order) {
        int id = order.getId();
        if (order.getId() == 0) {
            id = count.addAndGet(1);
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
    
    public Ticket4jOrder getOrder(int id) {
        return ORDERS.get(id);
    }
}
