(function($) {
	
	'use strict';
	
	var order = {
		init : function() {
			this.addEventListener();
		},
		
		updateStatus : function(data, tr) {
			var statusCode = data.statusCode;
			var message = data.message;
			
			var $tr = tr;
			var $status = $tr.find('.status');
			var $message = $('<span class="label"></span>');
			$message.html(message);
			
			$tr.find('.stop').hide();
			$tr.find('.start').hide();
			$tr.find('.remove').hide();
			
			if (statusCode == 1) { // Completed
				$message.addClass('label-success');
				$tr.find('.remove').show();
			}
			else if (statusCode == 0) { // Process
				$message.addClass('label-primary');
				$tr.find('.stop').show();
			}
			else if (statusCode == 2) { // Stop
				$message.addClass('label-default');
				$tr.find('.start').show();
			}
			else if (statusCode == 4) { // Wait
				$message.addClass('label-info');
			}
			else {
				$message.addClass('label-danger');
				$tr.find('.start').show();
			}
			$status.empty();
			$status.append($message);
		},
		
		addEventListener: function() {
			$(window).bind('ORDER_EVENT', function(evt, data) {
				var $tr = $('#' + data.id);
				order.updateStatus(data, $tr);
			});
			
			var popover= function(sessions) {
				var body = $('<div>');
				var content = $('<div>');
				content.append('<span>请选择需要购票的账号：</span>');
				content.append('<ul class="users"></ul>');
				$(sessions).each(function(i, item) {
					var li = $('<li class="users-internal"></li>');
					var btn = $('<button class="btn btn-default btn-sm btnAddOrder" data-username="'+item.username+'">'+item.username+'</button>');
					content.find('ul').append(li.append(btn));
				});
				body.append(content);
				$('#btnAddTicket').popover({
					html : true,
					content : CollectionUtils.isEmpty(sessions) ? '没有已经登陆的账号，请先登陆' : body.html()
				}).on('shown.bs.popover', function() {
					$('button.btnAddOrder').each(function() {
						var btn = $(this);
						btn.bind('click', function() {
							var session = $.session.getSessionByUsername($(this).data('username'));
							if (null == session) {
								$.notifier.error('无效的账号会话，请重新登陆后再试。');
								return;
							}
							order.openTicket(session);
						});
					});
				});
			}
			popover([]);
			
			$(window).bind('sessions.change', function(evt, sessions) {
				$('#btnAddTicket').popover('destroy');
				popover(sessions);
			});
			
			$(window).bind('create.order', function(evt, entity) {
				order.remote.createOrderForTask(entity);
			});
			$(window).bind('order.render', function(evt, entity) {
				order.createOrderForRender(entity);
			});
			$(window).bind('order.list.successful', function(evt, orders) {
				$(orders).each(function(i, item) {
					$(window).trigger('order.render', [ item ]);
				});
			});
			$('#tblTickets').bind('change', function(evt) {
				var len = $('#tblTickets').find('tbody tr').length;
				if (len > 1) {
					$('#tblTickets .empty').addClass('hide');
				}
				else {
					$('#tblTickets .empty').removeClass('hide');
				}
			});
			$(window).bind('order.remove.successful', function(evt, id, data) {
				$('#'+id).remove();
			});
			
			this.remote.list();
		},
		
		remote : {
			createOrderForTask : function(entity) {
				Ajax.post(window.baseUrl + '/order!create.shtml', {
					order : JSON.stringify(entity)
				}, function(json) {
					if (Response.ok(json)) {
						$.notifier.success('订单添加成功，系统将开始进行购票。');
						entity.id = json.Data.id;
						$(window).trigger('order.render', [ entity ]);
						$('#tblTickets').trigger('change');
						Modal.close();
					}
					else {
						$.notifier.error('订单添加失败！' + json.Message);
					}
				});
			},
			start: function(id) {
				Ajax.get(window.baseUrl + '/order!start.shtml', {
					id : id
				}, function(json) {
					if (Response.ok(json)) {
						$(window).trigger('order.start.successful', [ json.Data ]);
					}
					else {
						$.notifier.error('订单开始处理失败！' + json.Message);
					}
				});
			},
			stop: function(id) {
				Ajax.get(window.baseUrl + '/order!stop.shtml', {
					id : id
				}, function(json) {
					if (Response.ok(json)) {
						$(window).trigger('order.stop.successful', [ json.Data ]);
					}
					else {
						$.notifier.error('订单停止处理失败！' + json.Message);
					}
				});
			},
			remove: function(id) {
				Ajax.get(window.baseUrl + '/order!remove.shtml', {
					id : id
				}, function(json) {
					if (Response.ok(json)) {
						$(window).trigger('order.remove.successful', [ id, json.Data ]);
					}
					else {
						$.notifier.error('订单删除失败！' + json.Message);
					}
				});
			},
			list: function() {
				Ajax.get(window.baseUrl + '/order!list.shtml', {}, function(json) {
					if (Response.ok(json)) {
						$(window).trigger('order.list.successful', [ json.Data ]);
						$('#tblTickets').trigger('change');
					}
					else {
						$.notifier.error('无法获取已创建的订单！' + json.Message);
					}
				});
			}
		},
		
		openTicket: function(session) {
			var dialog = 
			$('<form class="form-horizontal">'+
			'  <div class="form-group">'+
			'	<label class="col-sm-2 control-label">乘车日期</label>'+
			'	<div class="col-sm-3">'+
			'		<input class="form-control" id="trainDate" size="18" data-date-format="yyyy-mm-dd" data-link-format="yyyy-mm-dd" readonly>'+
			'	</div>'+
			'  </div>'+
			'  <div class="form-group">'+
			'	<label class="col-sm-2 control-label">出发站</label>'+
			'	<div class="col-sm-3">'+
			'		<input class="form-control" id="start" placeholder="汉字">'+
			'	</div>'+
			'  </div>'+
			'  <div class="form-group">'+
			'	<label class="col-sm-2 control-label">到达站</label>'+
			'	<div class="col-sm-3">'+
			'		<input class="form-control" id="end" placeholder="汉字">'+
			'	</div>'+
			'  </div>'+
			'  <div class="form-group">'+
			'	<label class="col-sm-2 control-label">指定车次</label>'+
			'	<div class="col-sm-9">'+
			'		<div class="input-group col-sm-3">'+
			'		  <input type="text" class="form-control" id="iptIncludes">'+
			'		  <span class="input-group-btn">'+
			'			<button id="btnAddIncludes" class="btn btn-default" type="button">添加</button>'+
			'		  </span>'+
			'		</div>'+
			'		<span class="help-block">添加后将只会预订包含在内的车次，不添加则默认预订所有车次</span>'+
			'	</div>'+
			'  </div>'+
			'  <div id="div-includes" class="form-group hide">'+
			'	<div class="col-sm-offset-2 col-sm-9">'+
			'		<ul id="includes" class="list">'+
			'		</ul>'+
			'	</div>'+
			'  </div>'+
			'  <div id="control-excludes" class="form-group">'+
			'	<label class="col-sm-2 control-label">黑名单</label>'+
			'	<div class="col-sm-9">'+
			'		<div class="input-group col-sm-3">'+
			'		  <input type="text" class="form-control" id="iptExcludes">'+
			'		  <span class="input-group-btn">'+
			'			<button id="btnAddExcludes" class="btn btn-danger" type="button">添加</button>'+
			'		  </span>'+
			'		</div>'+
			'		<span class="help-block">添加后不会预订包含在内的车次</span>'+
			'	</div>'+
			'  </div>'+
			'  <div id="div-excludes" class="form-group hide">'+
			'	<div class="col-sm-offset-2 col-sm-9">'+
			'		<ul id="excludes" class="list"></ul>'+
			'	</div>'+
			'  </div>'+
			'  <div class="form-group">'+
			'	<label class="col-sm-2 control-label">指定席别</label>'+
			'	<div class="col-sm-10">'+
			'		<label class="checkbox-inline"><input name="seatType" type="checkbox" value="SWZ"> 商务座</label>'+
			'		<label class="checkbox-inline"><input name="seatType" type="checkbox" value="TDZ"> 特等座</label>'+
			'		<label class="checkbox-inline"><input name="seatType" type="checkbox" value="YDZ"> 一等座</label>'+
			'		<label class="checkbox-inline"><input name="seatType" type="checkbox" value="EDZ"> 二等座</label>'+
			'		<label class="checkbox-inline"><input name="seatType" type="checkbox" value="GJRW"> 高级软卧</label>'+
			'		<label class="checkbox-inline"><input name="seatType" type="checkbox" value="RW"> 软卧</label>'+
			'		<label class="checkbox-inline"><input name="seatType" type="checkbox" value="YW"> 硬卧</label>'+
			'		<label class="checkbox-inline"><input name="seatType" type="checkbox" value="RZ"> 软座</label>'+
			'		<label class="checkbox-inline"><input name="seatType" type="checkbox" value="YZ"> 硬座</label>'+
			'		<label class="checkbox-inline"><input name="seatType" type="checkbox" value="WZ"> 无座</label>'+
			'		<span class="help-block">将只会预订已选择的席别，靠前的席别优先预订。如果不指定则预订任意席别</span>'+
			'	</div>'+
			'  </div>'+
			'  <div id="div-seatTypes" class="form-group hide">'+
			'	<label class="col-sm-2 control-label">席别顺序</label>'+
			'	<div class="col-sm-9">'+
			'		<div class="row">'+
			'			<div class="col-sm-12">'+
			'				<ul id="selectedSeatType" class="list move"></ul>'+
			'			</div>'+
			'		</div>'+
			'		<span class="help-block">拖动可调整席别的顺序</span>'+
			'	</div>'+
			'  </div>'+
			'  <div id="div-order" class="form-group hide">'+
			'	<label class="col-sm-2 control-label">优先条件</label>'+
			'	<div class="col-sm-9">'+
			'     <label class="radio-inline">'+
			'       <input type="radio" name="orderBy" id="orderTrain" value="ORDER_TRAIN"> 按指定车次优先'+
			'     </label>'+
			'     <label class="radio-inline">'+
			'       <input type="radio" name="orderBy" id="orderSeat" value="ORDER_SEAT" checked="checked"> 按席别顺序优先'+
			'     </label>'+
			'	</div>'+
			'  </div>'+
			'  <div class="form-group">'+
			'	<label class="col-sm-2 control-label">乘车人</label>'+
			'	<div class="col-sm-9">'+
			'		<table id="tblPassenger" class="table table-striped table-bordered table-condensed">'+
			'			<thead>'+
			'				<tr>'+
			'					<th></th>'+
			'					<th>姓名</th>'+
			'					<th>身份证</th>'+
			'					<th>旅客类型</th>'+
			'				</tr>'+
			'			</thead>'+
			'			<tbody>'+
			'			</tbody>'+
			'		</table>'+
			'	</div>'+
			'  </div>'+
			'  <div class="form-group">' +
			'    <div class="col-sm-offset-4 col-sm-4"><button id="btnCreateOrder" type="button" class="btn btn-primary btn-lg btn-block" data-loading-text="正在添加订单...">确认添加</button></div>' +
			'  </div>' +
			'</form>');
			var body = dialog;
			Modal.open({
				title : '添加订单',
				body : body,
				backdrop : 'static',
				okBtn : 'hide',
				closeBtn : 'hide',
				size : 'lg',
				shown : function() {
					var today = new Date().Format('yyyy-MM-dd');
					$('#trainDate').val(today);
					$('#trainDate').datetimepicker({
				        language:  'zh-CN',
				        weekStart: 1,
				        todayBtn:  1,
						autoclose: 1,
						todayHighlight: 1,
						startView: 2,
						minView: 2,
						forceParse: 0
				    });
					$('#trainDate').datetimepicker('setStartDate', today);
					var date = new Date();
					date.setDate(date.getDate() + 19);
					$('#trainDate').datetimepicker('setEndDate', date.Format('yyyy-MM-dd'));
					
					var stationArray = [];
					var stations = station_names.split('@');
					$(stations).each(function(index, entry) {
						if (StringUtils.isNotBlank(entry)) {
							var items = entry.split('|');
							stationArray.push({
								primary: items[0],
								name: items[1],
								code: items[2],
								full: items[3],
								simple: items[4],
								index: items[5]
							});
						}
					});
					// 出发到达自动补全事件
					$('#start').autocomplete(stationArray, {
						formatItem: function(item, index, max) {
							return item.name;
						},
						formatMatch: function(row, index, max) {
							return row.name;
						}
					}).result(function(event, item) {
						$('#start').val(item.name);
						$('#end').focus();
					});
					$('#end').autocomplete(stationArray, {
						formatItem: function(item, index, max) {
							return item.name;
						},
						formatMatch: function(row, index, max) {
							return row.name;
						}
					}).result(function(event, item) {
						$('#end').val(item.name);
						$('#iptIncludes').focus();
					});
					
					
					var getAllowRmoveLi = function(text, clickHandler) {
						var li = $('<li class="autohide" data-value="'+text+'"><span>'+text+'</span><a href="javascript:;" class="glyphicon glyphicon-remove remove"></a></li>');
						li.find('a').click(function() {
							$(this).parent().remove();
							if (clickHandler) clickHandler();
						});
						return li;
					}
					
					// 指定车次事件
					$('#iptIncludes').keyup(function() {
						if (StringUtils.isNotBlank($(this).val())) {
							$(this).val($(this).val().toUpperCase());
						}
					}).keydown(function(evt) {
						if (evt.keyCode == 13) {
							$('#btnAddIncludes').trigger('click');
						}
					});
					$('#btnAddIncludes').click(function() {
						var val = $('#iptIncludes').val();
						if (StringUtils.isBlank(val))
							return;
						$('#iptIncludes').val('');
						var li = getAllowRmoveLi(val, function() {
							if ($('#includes').children().length == 0) {
								$('#div-includes').addClass('hide');
								$('#div-order').addClass('hide');
								$('#control-excludes').removeClass('hide');
								$('#orderSeat').prop('checked', true);
							}
						});
						$('#includes').append(li);
						if ($('#includes').children().length > 0) {
							$('#div-includes').removeClass('hide');
							$('#div-order').removeClass('hide');
							$('#control-excludes').addClass('hide');
						}
					});
					// 排除车次事件
					$('#iptExcludes').keyup(function() {
						if (StringUtils.isNotBlank($(this).val())) {
							$(this).val($(this).val().toUpperCase());
						}
					}).keydown(function(evt) {
						if (evt.keyCode == 13) {
							$('#btnAddExcludes').trigger('click');
						}
					});
					$('#btnAddExcludes').click(function() {
						var val = $('#iptExcludes').val();
						if (StringUtils.isBlank(val))
							return;
						$('#iptExcludes').val('');
						var li = getAllowRmoveLi(val, function() {
							if ($('#excludes').children().length == 0)
								$('#div-excludes').addClass('hide');
						});
						$('#excludes').append(li);
						if ($('#excludes').children().length > 0) {
							$('#div-excludes').removeClass('hide');
						}
					});
					
					// 席别选择事件
					$('input[name="seatType"]').change(function() {
						var seatType = $(this).val();
						if ($(this).prop('checked')) {
							$('#selectedSeatType').sortable('destroy');
							$('#selectedSeatType').append('<li data-value="'+seatType+'">'+$(this).parent().text()+'</li>');
							$('#selectedSeatType').sortable();
						}
						else {
							$('#selectedSeatType li').each(function() {
								var value = $(this).data('value');
								if (value == seatType) {
									$(this).remove();
								}
							});
						}
						if ($('#selectedSeatType').children().length > 0) {
							$('#div-seatTypes').removeClass('hide');
						}
						else {
							$('#div-seatTypes').addClass('hide');
						}
					});
					$('#tblPassenger').bind('render', function(evt, data) {
						if (CollectionUtils.isNotEmpty(data)) {
							$(data).each(function(i, item) {
								var tr = $('<tr>'+
					    				'<td class="center"><input type="checkbox" value="'+item.passenger_name+'_'+item.passenger_id_no+'_'+item.passenger_type+'" data-name="'+item.passenger_name+'"></td>'+ // name_id
					    				'<td>'+item.passenger_name+'</td>'+
					    				'<td>'+item.passenger_id_no+'</td>'+
					    				'<td>'+item.passenger_type_name+'</td>'+
					    			'</tr>');
								$('#tblPassenger tbody').append(tr);
							});
						}
					});
					$('#tblPassenger').trigger('render', [ session.passenger ]);
					$('#btnCreateOrder').bind('click', function() {
						var $trainDate = $('#trainDate');
						var $start = $('#start');
						var $end = $('#end');
						var $includes = $('#includes');
						var $excludes = $('#excludes');
						var $selectedSeatType = $('#selectedSeatType');
						var $tblPassenger = $('#tblPassenger');
						
						if (StringUtils.isBlank($trainDate.val())) {
							$.notifier.info('请选择乘车日期。');
							return;
						}
						if (StringUtils.isBlank($start.val())) {
							$.notifier.info('请输入出发站。');
							$start.focus();
							return;
						}
						if (StringUtils.isBlank($end.val())) {
							$.notifier.info('请输入到达站。');
							$end.focus();
							return;
						}
						
						var includes = [];
						$includes.find('li').each(function() {
							var no = $(this).data('value');
							includes.push(no);
						});
						
						var excludes = [];
						$excludes.find('li').each(function() {
							var no = $(this).data('value');
							excludes.push(no);
						});
						
						var seatTypes = [];
						$selectedSeatType.find('li').each(function() {
							var name = $(this).text().trim();
							var seat = $(this).data('value');
							seatTypes.push({
								name : name, type : seat
							});
						});
						
						var orderBy = $('input[name="orderBy"]:checked').val();
						
						var passengers = [];
						$tblPassenger.find('tbody input:checked').each(function() {
							passengers.push({
								name : $(this).data('name'),
								value : $(this).val()
							});
						});
						if (CollectionUtils.isEmpty(passengers)) {
							$.notifier.info('请选择乘车人。');
							return;
						}
						if (passengers.length > 5) {
							$.notifier.info('每次最多只能购买5张车票。');
							return;
						}
						var order = {
							session : session,
							trainDate : $trainDate.val(),
							startStation : $start.val(),
							endStation : $end.val(),
							includes : includes,
							excludes : excludes,
							seatTypes : seatTypes,
							orderBy : orderBy,
							passengers : passengers
						};
						$(window).trigger('create.order', [ order ]);
					});
				},
				hiden : function() {}
			});
		},
		
		createOrderForRender : function(entity) {
			var tr = $('<tr>');
			tr.attr('id', entity.id);
			
			var trainDate = $('<td>'+entity.trainDate+'</td>');
			var startStation = $('<td>'+entity.startStation+'</td>');
			var endStation = $('<td>'+entity.endStation+'</td>');
			
			var includes = $('<td></td>');
			if (CollectionUtils.isNotEmpty(entity.includes)) {
				includes.append('<ul class="simple-list"></ul>');
				$(entity.includes).each(function(i, item) {
					includes.find('ul').append('<li>' + item + '</li>');
				});
			} else {
				includes.html('<span class="label label-info">无</span>');
			}
			
			var excludes = $('<td></td>');
			if (CollectionUtils.isNotEmpty(entity.excludes)) {
				excludes.append('<ul class="simple-list"></ul>');
				$(entity.excludes).each(function(i, item) {
					excludes.find('ul').append('<li>' + item + '</li>');
				});
			} else {
				excludes.html('<span class="label label-info">无</span>');
			}
			
			var seatTypes = $('<td></td>');
			if (CollectionUtils.isNotEmpty(entity.seatTypes)) {
				seatTypes.append('<ul class="simple-list"></ul>');
				$(entity.seatTypes).each(function(i, item) {
					seatTypes.find('ul').append('<li>' + item.name + '</li>');
				});
			} else {
				seatTypes.html('<span class="label label-info">全部席别</span>');
			}
			
			var passengers = $('<td><ul class="simple-list"></ul></td>');
			$(entity.passengers).each(function(i, item) {
				passengers.find('ul').append('<li>' + item.name + '</li>');
			});
			var status = $('<td class="status"></td>');
			status.append('<span class="label label-success">正在初始化</span>');
			
			var opera = $('<td>');
			
			var $start = $('<a href="javascript:;" class="start glyphicon glyphicon-play" title="开始处理"></a>');
			$start.data('id', entity.id);
			$start.bind('click', function() {
				var id = $(this).data('id');
				order.remote.start(id);
			});
			
			var $stop = $('<a href="javascript:;" class="stop glyphicon glyphicon-stop" title="停止处理"></a>');
			$stop.data('id', entity.id);
			$stop.bind('click', function() {
				var id = $(this).data('id');
				Alert.confirm('您确定要停止处理这个订单吗？', function() {
					order.remote.stop(id);
				});
			});
			
			var $remove = $('<a href="javascript:;" class="remove glyphicon glyphicon-trash" title="删除"></a>');
			$remove.data('id', entity.id);
			$remove.bind('click', function() {
				var id = $(this).data('id');
				Alert.confirm('您确定要删除处理这个订单吗？', function() {
					order.remote.remove(id);
				});
			});
			
			opera.append($start).append($stop).append($remove);
			
			tr.append(trainDate).append(startStation).append(endStation).append(includes).append(excludes).append(seatTypes).append(passengers).append(status).append(opera);
			$('#tblTickets tbody').append(tr);
			
			order.updateStatus(entity, tr);
		}
	}
	
	$(function() {
		order.init();
	});
	
})(jQuery);