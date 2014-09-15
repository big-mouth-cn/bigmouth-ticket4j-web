package org.bigmouth.ticket4jweb.ticket.process;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

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
import org.bigmouth.ticket4jweb.ticket.service.OrderService;
import org.bigmouth.ticket4jweb.ticket.service.SessionService;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.gson.Gson;


public class OrderProcessFactory extends BaseLifeCycleSupport {

    private static final long MILLIS = 1000;
    
    private final OrderService orderService;
    private final SessionService sessionService;
    private final ExecutorService executor;
    
    private Ticket ticket;
    private Order order;
    private PassCode passCode;
    private AntiUtils antiUtils;
    private TicketReport ticketReport;
    
    private WebSocketFactory webSocketFactory;
    
    public OrderProcessFactory(OrderService orderService, SessionService sessionService) {
        Preconditions.checkNotNull(orderService, "orderService");
        Preconditions.checkNotNull(sessionService, "sessionService");
        this.orderService = orderService;
        this.sessionService = sessionService;
        this.executor = Executors.newCachedThreadPool(new ThreadFactory() {
            
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setName("Order");
                return thread;
            }
        });
    }
    
    
    public void putPool(final int id) {
        executor.submit(new Runnable() {
            
            @Override
            public void run() {
                Ticket4jOrder ticket4jOrder = orderService.getOrder(id);
                Preconditions.checkNotNull(ticket4jOrder, "订单无效。");
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
                
                List<Train> allows = null;
                do {
                    print(ticket4jOrder, "正在查票");
                    QueryTicketResponse queryTicketResponse = ticket.query(ticket4jHttpResponse, condition);
                    if (null == queryTicketResponse)
                        continue;
                    allows = queryTicketResponse.getAllows();
                    if (CollectionUtils.isEmpty(allows)) {
                        print(ticket4jOrder, Ticket4jOrder.NO_TICKET, "暂时没有符合预订条件的车次。");
                        sleep(MILLIS);
                    }
                } while (CollectionUtils.isEmpty(allows));
                
                // 开始订票
                for (Train train : allows) {
                    SubmitOrderRequest submitOrderRequest = new SubmitOrderRequest(trainDate, trainDate, condition.getPurposeCodes(), train);
                    Response submitResponse = null;
                    TrainDetails details = train.getQueryLeftNewDTO();
                    do {
                        print(ticket4jOrder, String.format("正在预订 %s 的车票", details.getStation_train_code()));
                        submitResponse = order.submit(ticket4jHttpResponse, submitOrderRequest);
                        if (null == submitResponse) 
                            continue;
                        else {
                            if (StringUtils.startsWith(submitResponse.getMessage(), "您还有未处理的订单")) {
                                print(ticket4jOrder, Ticket4jOrder.NO_COMPLETE_ORDER, "您还有未处理的订单。");
                                return;
                            }
                        }
                    } while (!submitResponse.isContinue());
                    
                    List<Seat> canBuySeats = train.getCanBuySeats(); // 允许购买的席别
                    Seat seat = canBuySeats.get(0);
                    
                    Token token = order.getToken(ticket4jHttpResponse);
                    
                    StringBuilder passengerSource = new StringBuilder();
                    for (Ticket4jPassenger passenger : passengers) {
                        passengerSource.append(passenger.getValue()).append(",");
                    }
                    List<Person> persons = Person.of(StringUtils.substringBeforeLast(passengerSource.toString(), ","));
                    
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
                        print(ticket4jOrder, "正在获取订单验证码");
                        File orderPassCode = passCode.getOrderPassCode(ticket4jHttpResponse);
                        code = antiUtils.recognition(4, orderPassCode.getPath());
                        checkOrderInfoRequest.setRandCode(new String(code));
                        print(ticket4jOrder, "正在验证订单");
                        checkOrderInfo = order.checkOrderInfo(ticket4jHttpResponse, checkOrderInfoRequest);
                        if (null == checkOrderInfo) {
                            continue;
                        }
                        if (checkOrderInfo.isContinue()) {
                            break;
                        }
                        else {
                            print(ticket4jOrder, Ticket4jOrder.CANCEL_ORDER_MAX, checkOrderInfo.getMessage());
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
                            print(ticket4jOrder, String.format("恭喜！车票预订成功，请尽快使用 %s 登录12306客运服务后台进行支付。", session.getUsername()));
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
        orderService.write(ticket4jOrder);
        print(ticket4jOrder);
    }
    
    private void print(Ticket4jOrder order) {
        WebSocketMessage<Ticket4jOrder> message = new WebSocketMessage<Ticket4jOrder>(WebSocketMessageType.ORDER_EVENT, order);
        String text = new Gson().toJson(message);
        webSocketFactory.getWebSocket().print(text);
    }

    @Override
    protected void doDestroy() {
        executor.shutdownNow();
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
