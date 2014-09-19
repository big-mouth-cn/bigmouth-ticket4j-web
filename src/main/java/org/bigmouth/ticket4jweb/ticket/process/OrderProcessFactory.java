package org.bigmouth.ticket4jweb.ticket.process;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.bigmouth.framework.util.BaseLifeCycleSupport;
import org.bigmouth.framework.web_socket.WebSocketFactory;
import org.bigmouth.ticket4j.Order;
import org.bigmouth.ticket4j.PassCode;
import org.bigmouth.ticket4j.Ticket;
import org.bigmouth.ticket4j.entity.Person;
import org.bigmouth.ticket4j.entity.Response;
import org.bigmouth.ticket4j.entity.Seat;
import org.bigmouth.ticket4j.entity.Token;
import org.bigmouth.ticket4j.entity.request.CheckOrderInfoRequest;
import org.bigmouth.ticket4j.entity.request.ConfirmSingleForQueueRequest;
import org.bigmouth.ticket4j.entity.request.QueryTicketRequest;
import org.bigmouth.ticket4j.entity.request.QueueCountRequest;
import org.bigmouth.ticket4j.entity.request.SubmitOrderRequest;
import org.bigmouth.ticket4j.entity.response.CheckOrderInfoResponse;
import org.bigmouth.ticket4j.entity.response.ConfirmSingleForQueueResponse;
import org.bigmouth.ticket4j.entity.response.NoCompleteOrderResponse;
import org.bigmouth.ticket4j.entity.response.OrderWaitTimeResponse;
import org.bigmouth.ticket4j.entity.response.QueryTicketResponse;
import org.bigmouth.ticket4j.entity.response.QueueCountResponse;
import org.bigmouth.ticket4j.entity.train.Train;
import org.bigmouth.ticket4j.entity.train.TrainDetails;
import org.bigmouth.ticket4j.http.Ticket4jHttpResponse;
import org.bigmouth.ticket4j.report.Report;
import org.bigmouth.ticket4j.report.TicketReport;
import org.bigmouth.ticket4j.utils.AntiUtils;
import org.bigmouth.ticket4j.utils.PersonUtils;
import org.bigmouth.ticket4j.utils.StationUtils;
import org.bigmouth.ticket4jweb.ticket.entity.Session;
import org.bigmouth.ticket4jweb.ticket.entity.Ticket4jOrder;
import org.bigmouth.ticket4jweb.ticket.entity.Ticket4jPassenger;
import org.bigmouth.ticket4jweb.ticket.entity.Ticket4jSeat;
import org.bigmouth.ticket4jweb.ticket.entity.WebSocketMessage;
import org.bigmouth.ticket4jweb.ticket.entity.WebSocketMessageType;
import org.bigmouth.ticket4jweb.ticket.service.NotificationService;
import org.bigmouth.ticket4jweb.ticket.service.OrderService;
import org.bigmouth.ticket4jweb.ticket.service.SessionService;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;


public class OrderProcessFactory extends BaseLifeCycleSupport {

    private static final long MILLIS = 1000;
    private static final int MAX_ORDER_PROCESS_THREAD = 5;
    
    private static final Map<String, Ticket4jOrder> PROCESS_QUEUE = Maps.newConcurrentMap();
    private static final Map<String, Ticket4jOrder> STOP_QUEUE = Maps.newConcurrentMap();
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(MAX_ORDER_PROCESS_THREAD, Executors.defaultThreadFactory());
    
    private final OrderService orderService;
    private final SessionService sessionService;
    private final NotificationService notificationService;
    
    private Ticket ticket;
    private Order order;
    private PassCode passCode;
    private AntiUtils antiUtils;
    private TicketReport ticketReport;
    
    private WebSocketFactory webSocketFactory;
    
    public OrderProcessFactory(OrderService orderService, SessionService sessionService, NotificationService notificationService) {
        Preconditions.checkNotNull(orderService, "orderService");
        Preconditions.checkNotNull(sessionService, "sessionService");
        Preconditions.checkNotNull(notificationService, "notificationService");
        this.orderService = orderService;
        this.sessionService = sessionService;
        this.notificationService = notificationService;
    }
    
    public void putStopQueue(String id) {
        Ticket4jOrder ticket4jOrder = orderService.getOrder(id);
        STOP_QUEUE.put(id, ticket4jOrder);
    }
    
    public void putPool(final String id) {
        final Ticket4jOrder ticket4jOrder = orderService.getOrder(id);
        Preconditions.checkNotNull(ticket4jOrder, "订单无效。");
        print(ticket4jOrder, Ticket4jOrder.WAIT, "等待进入处理队列");
        
        EXECUTOR.submit(new Runnable() {
            
            @Override
            public void run() {
                if (PROCESS_QUEUE.containsKey(id)) {
                    print(ticket4jOrder, Ticket4jOrder.REPEAT, "订单已经在处理队列中");
                    return;
                }
                PROCESS_QUEUE.put(id, ticket4jOrder);
                
                Session session = ticket4jOrder.getSession();
                if (null == session) {
                    print(ticket4jOrder, Ticket4jOrder.NULL, "无效的订单，没有账号会话。");
                }
                Session crt = sessionService.get(session.getUsername());
                if (!crt.isSignIn()) {
                    print(ticket4jOrder, Ticket4jOrder.SESSION_TIME_OUT, "账户会话已经超时，需要重新登陆并创建订单。");
                }
                Ticket4jHttpResponse ticket4jHttpResponse = crt.getTicket4jHttpResponse();
                String trainDate = ticket4jOrder.getTrainDate();
                String trainFrom = ticket4jOrder.getStartStation();
                String trainTo = ticket4jOrder.getEndStation();
                String[] includes = ticket4jOrder.getIncludes();
                String[] excludes = ticket4jOrder.getExcludes();
                Ticket4jSeat[] seatTypes = ticket4jOrder.getSeatTypes();
                List<Seat> seats = Lists.newArrayList();
                if (ArrayUtils.isEmpty(seatTypes)) {
                    seats = Lists.newArrayList(Seat.ALL);
                }
                else {
                    for (Ticket4jSeat ticket4jSeat : seatTypes) {
                        seats.add(ticket4jSeat.getType());
                    }
                }
                Ticket4jPassenger[] passengers = ticket4jOrder.getPassengers();
                if (ArrayUtils.isEmpty(passengers)) {
                    print(ticket4jOrder, Ticket4jOrder.NULL, "没有设置乘车人。");
                    PROCESS_QUEUE.remove(id);
                    return;
                }
                
                // 查票
                QueryTicketRequest condition = new QueryTicketRequest();
                condition.setTrainDate(trainDate);
                condition.setFromStation(StationUtils.find(trainFrom));
                condition.setToStation(StationUtils.find(trainTo));
                condition.setIncludeTrain(Lists.newArrayList(includes));
                condition.setExcludeTrain(Lists.newArrayList(excludes));
                condition.setSeats(seats);
                condition.setTicketQuantity(passengers.length);
                condition.setOrderBy(ticket4jOrder.getOrderBy());
                
                List<Train> allows = null;
                do {
                    if (STOP_QUEUE.containsKey(id)) {
                        PROCESS_QUEUE.remove(id);
                        STOP_QUEUE.remove(id);
                        print(ticket4jOrder, Ticket4jOrder.STOP, "订单已停止");
                        return;
                    }
                    print(ticket4jOrder, "正在查票中");
                    try {
                        QueryTicketResponse queryTicketResponse = ticket.query(ticket4jHttpResponse, condition);
                        if (null == queryTicketResponse)
                            continue;
                        allows = queryTicketResponse.getAllows();
                        if (CollectionUtils.isNotEmpty(allows)) {
                            break;
                        }
                        sleep(MILLIS);
                    }
                    catch (Exception e) {
                        print(ticket4jOrder, Ticket4jOrder.RUNTIME, e.getMessage());
                    }
                } while (CollectionUtils.isEmpty(allows));
                
                // 开始订票
                for (Train train : allows) {
                    SubmitOrderRequest submitOrderRequest = new SubmitOrderRequest(trainDate, trainDate, condition.getPurposeCodes(), train);
                    Response submitResponse = null;
                    TrainDetails details = train.getQueryLeftNewDTO();
                    do {
                        print(ticket4jOrder, String.format("正在预订 %s 的车票", details.getStation_train_code()));
                        try {
                            submitResponse = order.submit(ticket4jHttpResponse, submitOrderRequest);
                        }
                        catch (Exception e) {
                            print(ticket4jOrder, Ticket4jOrder.RUNTIME, e.getMessage());
                        }
                        if (null == submitResponse) 
                            continue;
                        else {
                            if (StringUtils.startsWith(submitResponse.getMessage(), "您还有未处理的订单")) {
                                print(ticket4jOrder, Ticket4jOrder.NO_COMPLETE_ORDER, "您还有未处理的订单。");
                                PROCESS_QUEUE.remove(id);
                                return;
                            }
                        }
                    } while (!submitResponse.isContinue());
                    
                    List<Seat> canBuySeats = train.getCanBuySeats(); // 允许购买的席别
                    Seat seat = canBuySeats.get(0);
                    
                    Token token = null;
                    try {
                        token = order.getToken(ticket4jHttpResponse);
                    }
                    catch (Exception e) {
                        print(ticket4jOrder, Ticket4jOrder.RUNTIME, e.getMessage());
                    }
                    if (null == token) {
                        print(ticket4jOrder, Ticket4jOrder.NULL, "无法获取Token密钥。");
                        PROCESS_QUEUE.remove(id);
                        return;
                    }
                    
                    StringBuilder passengerSource = new StringBuilder();
                    for (Ticket4jPassenger passenger : passengers) {
                        passengerSource.append(passenger.getValue()).append(",");
                    }
                    List<Person> persons = Person.of(StringUtils.substringBeforeLast(passengerSource.toString(), ","), false);
                    
                    // 检查订单完整性
                    String seatTypesValue = details.getSeat_types();
                    String passengerTicketStr = PersonUtils.toPassengerTicketStr(persons, seat, seatTypesValue);
                    String oldPassengerTicket = PersonUtils.toOldPassengerTicket(persons, seat, seatTypesValue);
                    
                    CheckOrderInfoRequest checkOrderInfoRequest = new CheckOrderInfoRequest();
                    checkOrderInfoRequest.setRepeatSubmitToken(token.getToken());
                    checkOrderInfoRequest.setPassengerTicketStr(passengerTicketStr);
                    checkOrderInfoRequest.setOldPassengerStr(oldPassengerTicket);
                    CheckOrderInfoResponse checkOrderInfo = null;
                    byte[] code = null;
                    do {
                        File orderPassCode = null;
                        try {
                            orderPassCode = passCode.getOrderPassCode(ticket4jHttpResponse);
                        }
                        catch (Exception e) {
                            print(ticket4jOrder, Ticket4jOrder.RUNTIME, e.getMessage());
                            PROCESS_QUEUE.remove(id);
                            return;
                        }
                        try {
                            Preconditions.checkNotNull(orderPassCode, "订单验证码获取失败。");
                            code = antiUtils.recognition(4, orderPassCode.getPath());
                        }
                        catch (Exception e) {
                            print(ticket4jOrder, Ticket4jOrder.RUNTIME, "验证码识别失败!" + e.getMessage());
                        }
                        checkOrderInfoRequest.setRandCode(new String(code));
                        print(ticket4jOrder, "正在验证订单");
                        
                        try {
                            checkOrderInfo = order.checkOrderInfo(ticket4jHttpResponse, checkOrderInfoRequest);
                            if (null == checkOrderInfo) {
                                continue;
                            }
                            if (checkOrderInfo.isContinue()) {
                                break;
                            }
                            else {
                                if (StringUtils.startsWith(checkOrderInfo.getMessage(), "对不起，由于您取消次数过多，今日将不能继续受理您的订票请求。")) {
                                    PROCESS_QUEUE.remove(id);
                                    print(ticket4jOrder, Ticket4jOrder.CANCEL_ORDER_MAX, "对不起，今日您取消次数过多，无法受理订票请求。");
                                    return;
                                }
                            }
                        }
                        catch (Exception e) {
                            print(ticket4jOrder, Ticket4jOrder.RUNTIME, e.getMessage());
                            PROCESS_QUEUE.remove(id);
                            return;
                        }
                    } while (!checkOrderInfo.isContinue());
                    
                    // 排队
                    QueueCountRequest queueCountRequest = new QueueCountRequest();
                    QueueCountResponse queueCountResponse = null;
                    queueCountRequest.setToken(token);
                    queueCountRequest.setTrainDate(trainDate);
                    queueCountRequest.setTrainDetails(details);
                    do {
                        queueCountResponse = order.getQueueCount(ticket4jHttpResponse, queueCountRequest);
                        if (queueCountResponse.isContinue())
                            break;
                        print(ticket4jOrder, String.format("当前排队人数：%s", queueCountResponse.getData().getCountT()));
                    } while (queueCountResponse == null || !queueCountResponse.isContinue());
                    
                    // 提交订单
                    ConfirmSingleForQueueRequest queueRequest = new ConfirmSingleForQueueRequest();
                    queueRequest.setKeyCheckIsChange(token.getOrderKey());
                    queueRequest.setLeftTicketStr(train.getQueryLeftNewDTO().getYp_info());
                    queueRequest.setPassengerTicketStr(passengerTicketStr);
                    queueRequest.setOldPassengerStr(oldPassengerTicket);
                    queueRequest.setRandCode(new String(code));
                    queueRequest.setRepeatSubmitToken(token.getToken());
                    queueRequest.setTrainLocation(train.getQueryLeftNewDTO().getLocation_code());
                    
                    ConfirmSingleForQueueResponse confirmResponse = null;
                    do {
                        print(ticket4jOrder, "正在提交订单");
                        confirmResponse = order.confirmSingleForQueue(ticket4jHttpResponse, queueRequest);
                        if (null == confirmResponse)
                            continue;
                        if (!confirmResponse.isContinue() && 
                                StringUtils.equals("包含未付款订单", confirmResponse.getErrorMessage())) {
                            print(ticket4jOrder, "包含未付款订单");
                            return;
                        }
                    } while (!confirmResponse.isContinue());
                    
                    // 等待处理结果
                    OrderWaitTimeResponse waitTimeResponse = new OrderWaitTimeResponse();
                    do {
                        waitTimeResponse = order.queryOrderWaitTime(ticket4jHttpResponse, token);
                        if (null == waitTimeResponse)
                            continue;
                        
                        if (!waitTimeResponse.isContinue()) {
                            int waitTime = waitTimeResponse.getData().getWaitTime();
                            print(ticket4jOrder, String.format("订单已经提交，大概还需要 %s 秒", waitTime));
                            sleep(1000);
                        }
                    } while (!waitTimeResponse.isContinue());
                    
                    // 没有足够的票
                    String orderId = waitTimeResponse.getData().getOrderId();
                    if (StringUtils.isBlank(orderId)) {
                        print(ticket4jOrder, Ticket4jOrder.WAIT_TIME, waitTimeResponse.getMessage());
                        PROCESS_QUEUE.remove(id);
                        continue;
                    }
                    
                    NoCompleteOrderResponse noComplete = new NoCompleteOrderResponse();
                    do {
                        noComplete = order.queryNoComplete(ticket4jHttpResponse);
                        if (null == noComplete)
                            continue;
                        if (noComplete.isContinue()) {
                            Report report = new Report();
                            report.setUsername(session.getUsername());
                            report.setOrders(noComplete.getData().getOrderDBList());
                            ticketReport.write(report);
                            notificationService.notification(report);
                            print(ticket4jOrder, Ticket4jOrder.COMPLETED, String.format("恭喜！(%s)车票预订成功，请尽快支付。", session.getUsername()));
                            PROCESS_QUEUE.remove(id);
                            return;
                        }
                    } while (!noComplete.isContinue());
                }
            }
        });
    }

    @Override
    protected void doInit() {
        // TODO
    }
    
    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        }
        catch (InterruptedException e) {
        }
    }
    
    private void print(Ticket4jOrder ticket4jOrder, String message) {
        print(ticket4jOrder, Ticket4jOrder.NORMAL, message);
    }
    
    private void print(Ticket4jOrder ticket4jOrder, int statusCode, String message) {
        ticket4jOrder.setStatusCode(statusCode);
        ticket4jOrder.setMessage(message);
        print(ticket4jOrder);
    }
    
    private void print(Ticket4jOrder order) {
        orderService.write(order);
        WebSocketMessage<Ticket4jOrder> message = new WebSocketMessage<Ticket4jOrder>(WebSocketMessageType.ORDER_EVENT, order);
        String text = new Gson().toJson(message);
        webSocketFactory.getWebSocket().print(text);
    }

    @Override
    protected void doDestroy() {
        EXECUTOR.shutdownNow();
    }
    
    public void setWebSocketFactory(WebSocketFactory webSocketFactory) {
        this.webSocketFactory = webSocketFactory;
    }

    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public void setPassCode(PassCode passCode) {
        this.passCode = passCode;
    }

    public void setAntiUtils(AntiUtils antiUtils) {
        this.antiUtils = antiUtils;
    }

    public void setTicketReport(TicketReport ticketReport) {
        this.ticketReport = ticketReport;
    }
}
