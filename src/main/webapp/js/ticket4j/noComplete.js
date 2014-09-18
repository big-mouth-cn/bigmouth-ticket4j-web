(function($) {
	
	'use strict';
	
	var noComplete = {
		init: function() {
			this.addEventListener();
		},
		
		addEventListener : function() {
			// open
			$(window).bind('noComplete.open', function(evt, session) {
				var body = noComplete.ui.dialog(session);
				Modal.open({
					title : session.username + ' 待支付的订单',
					body : body,
					backdrop : 'static',
					okBtn : 'hide',
					closeBtn : 'hide',
					size : 'lg',
					shown : function() {
						$(window).bind('noComplete.load.successful', function(e, data) {
							body.find('.loading').hide();
							if (!data.data) {
								body.find('.nodata').removeClass('hide');
								body.find('.nodata').html('加载失败，请关闭后重试');
							}
							var orderDBList = data.data.orderDBList;
							if (!orderDBList || orderDBList.length == 0) {
								body.find('.nodata').removeClass('hide');
								body.find('.nodata').html('暂时没有待支付的订单');
							}
							else {
								$(orderDBList).each(function(i, order) {
									var tickets = order.tickets;
									var order_date = order.order_date; // 订单日期
									var sequence_no = order.sequence_no; // 订单号
									var train_code_page = order.train_code_page; // 车次
									var start_train_date_page = order.start_train_date_page; // 出发时间
									var from = order.from_station_name_page[0]; // 出发站
									var to_station = order.to_station_name_page[0]; // 到达站
									
									var $table = $('<table class="table table-bordered">');
									var $tbody = $('<tbody>');
									var $head = $('<tr>');
									var $head_td = $('<td colspan="6" class="bg-primary">');
									
									$head_td.append('<span><b>'+train_code_page+'</b></span>&nbsp;&nbsp;'+start_train_date_page+'&nbsp;&nbsp;出发，从&nbsp;&nbsp;'+from+'&nbsp;&nbsp;开往&nbsp;&nbsp;'+to_station);
									$head.append($head_td);
									$tbody.append($head);
									
									$(tickets).each(function(j, ticket) {
										var passengerDTO = ticket.passengerDTO;
										var passenger_name = passengerDTO.passenger_name; // 姓名
										var passenger_id_type_name = passengerDTO.passenger_id_type_name; // 证件类型
										var coach_name = ticket.coach_name; // 车厢
										var seat_name = ticket.seat_name; // 席别号
										var seat_type_name = ticket.seat_type_name; // 席别类型
										var ticket_type_name = ticket.ticket_type_name; // 票种
										var str_ticket_price_page = ticket.str_ticket_price_page; // 价格
										var ticket_status_name = ticket.ticket_status_name; // 状态
										
										var $ticket = $('<tr>');
										var $name = $('<td>'); $name.text(passenger_name);
										var $seatType = $('<td>'); $seatType.text(seat_type_name);
										var $seat = $('<td>'); $seat.append(coach_name + ' 车厢&nbsp;' + seat_name);
										var $ticketType = $('<td>'); $ticketType.append(ticket_type_name);
										var $price = $('<td>'); $price.append(str_ticket_price_page + ' 元');
										var $status = $('<td>'); $status.append(ticket_status_name);
										
										$ticket.append($name).append($seatType).append($seat).append($ticketType).append($price).append($status);
										$tbody.append($ticket);
									});
									
									var $total = $('<tr>');
									var $total_td = $('<td colspan="6" align="right">');
									$total_td.append('<span class="label label-success" title="订单号">'+sequence_no+'</span>&nbsp;&nbsp;&nbsp;&nbsp;'+order_date);
									$tbody.append($total.append($total_td));
									$table.append($tbody);
									body.append($table);
								});
							}
						});
						$(window).bind('noComplete.load.failed', function(e, data) {
							console.log(data);
						});
						noComplete.request.noComplete(session.ticket4jHttpResponse);
					},
					hiden : function() {
						$(window).unbind('noComplete.load.successful');
						$(window).unbind('noComplete.load.failed');
					}
				});
			});
		},
		
		ui: {
			dialog : function(session) {
				var container =  $('<div>');
				var loading = $('<div class="loading"><div class="span">正在加载，请稍候...</div></div>');
				container.append(loading);
				var none = $('<div class="nodata alert alert-info hide"></div>');
				container.append(none);
				return container;
			}
		},
		
		request : {
			noComplete : function(ticket4jHttpResponse) {
				Ajax.get(window.baseUrl + '/order!noComplete.shtml', {
					ticket4jHttpResponse : JSON.stringify(ticket4jHttpResponse)
				}, function(json) {
					if (Response.ok(json)) {
						$(window).trigger('noComplete.load.successful', [ json.Data ]);
					}
					else {
						$(window).trigger('noComplete.load.failed', [ json ]);
					}
				});
			}
		}
	}
	
	$(function() {
		noComplete.init();;
	})
	
})(jQuery);