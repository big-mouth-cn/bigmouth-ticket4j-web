(function($) {
	
	'use strict';
	var address;
	
	var about = {
		init : function() {
			this.initNotification();
			this.addEventListener();
		},
		
		initNotification: function() {
			Ajax.get(window.baseUrl + '/notification.shtml', {}, function(json) {
				if (Response.ok(json)) {
					address = json.Data;
				}
				else {
					$.notifier.error('获取成功通知设置失败!' + json.Message);
				}
			});
		},
		
		addEventListener : function() {
			$('#about').click(function() {
				var body = 
					$('<div>'+
							'	<div class="text-center">'+
							'		<img class="img-responsive" src="'+window.baseUrl+'/images/logo.png" style="max-width: 200px; display: inline;">'+
							'	</div>'+
							'	<p><span>东皇钟</span>，取名自上古神器之一，它是一款购买火车票的软件。</p>'+
							'   <p>它主要目的是减少人机交互来帮助用户购票，其原理是通过HttpClient访问购票接口，不需要登录12306官方，不需要加载多余的CSS、JS脚本等文件。这样既能减轻12306服务器CDN网络的压力，同样也能够更快速的达到购票目的。</p>'+
							'   <p>东皇钟官方网址：<a href="http://www.big-mouth.cn/ticket4j" target="_blank">http://www.big-mouth.cn/ticket4j</a></p>' +
					'</div>');
				Modal.open({
					title : '关于东皇钟',
					body : body,
					backdrop : 'static',
					okBtn : 'hide',
					closeBtn : 'hide'
				});
			});
			$('#donations').click(function() {
				var body = 
					$('<div>'+
							'	<div class="text-center">'+
							'		<img class="img-responsive" src="'+window.baseUrl+'/images/alipay.jpg" style="max-width: 200px; display: inline; margin-bottom: 15px;">'+
							'	</div>'+
							'	<p>如果您确实觉得东皇钟帮助到了您，并且<b>愿意捐助任意金额</b>以支持鼓励作者继续保持更新。那么您可以打开“手机版支付宝客户端”，使用“扫一扫”功能扫描上面的二维码进行捐助。如果您没有手机版支付宝客户端，也可以登陆<a href="http://www.alipay.com" target="_blank">支付宝</a>进行捐助。谢谢！</p>'+
							'   <p>我的支付宝账号是：huxiao.mail@qq.com</p>'+
					'</div>');
				Modal.open({
					title : '捐助大嘴',
					body : body,
					backdrop : 'static',
					okBtn : 'hide',
					closeBtn : 'hide'
				});
			});
			$('#notification').click(function() {
				var body = 
					$('<div>'+
							'<form class="form-horizontal" role="form">'+
							'  <div class="form-group">'+
							'    <label class="col-sm-3 control-label">邮箱地址</label>'+
							'    <div class="col-sm-8">'+
							'      <input type="email" class="form-control" id="emailAddress" placeholder="接收成功通知的邮箱地址">'+
							'      <p class="help-block">当有火车票预订成功后，系统将会发送通知到此邮箱中。</p>' +
							'    </div>'+
							'  </div>'+
							'  <div class="form-group">'+
							'    <label class="col-sm-3 control-label">测试内容</label>'+
							'    <div class="col-sm-8">'+
							'      <textarea class="form-control" id="content" rows="5"></textarea>'+
							'    </div>'+
							'  </div>'+
							'  <div class="form-group">'+
							'    <div class="col-sm-offset-3 col-sm-8">'+
							'      <button id="btnSaveNotification" type="button" class="btn btn-primary">保存</button>'+
							'      <a id="btnSendEmail" type="button" class="btn btn-line">发送测试邮件</a>'+
							'    </div>'+
							'  </div>'+
							'  <hr>' +
							'  <div class="alert alert-info">推荐使用移动139或QQ邮箱，开启手机短信通知功能，这样当有邮件到达时可第一时间得知。具体设置方法请自行查找。</div>' +
							'</form>'+
					'</div>');
				Modal.open({
					title : '成功通知',
					body : body,
					backdrop : 'static',
					okBtn : 'hide',
					closeBtn : 'hide',
					shown : function() {
						var $emailAddress = $('#emailAddress');
						var $content = $('#content');
						if (!($.isEmptyObject(address)) && StringUtils.isNotBlank(address))
							$emailAddress.val(address);
						$emailAddress.focus();
						body.find('#btnSaveNotification').click(function() {
							if (StringUtils.isBlank($emailAddress.val())) {
								$emailAddress.focus();
								return;
							}
							Ajax.get(window.baseUrl + '/notification!save.shtml', {
								to : $emailAddress.val()
							}, function(json) {
								if (Response.ok(json)) {
									address = $emailAddress.val();
									$.notifier.success('成功通知接收邮箱保存成功');
								}
								else {
									$.notifier.error(json.Message);
								}
							});
						});
						body.find('#btnSendEmail').click(function() {
							if (StringUtils.isBlank($emailAddress.val())) {
								$emailAddress.focus();
								return;
							}
							if (StringUtils.isBlank($content.val())) {
								$content.focus();
								return;
							}
							
							Ajax.post(window.baseUrl + '/notification!send.shtml', {
								to : $emailAddress.val(),
								content : $content.val()
							}, function(json) {
								if (Response.ok(json)) {
									$.notifier.success('测试邮件发送成功');
								}
								else {
									$.notifier.error(json.Message);
								}
							});
						});
					}
				});
			});
		}
	};
	$(function() {
		about.init();
	});
	
})(jQuery);