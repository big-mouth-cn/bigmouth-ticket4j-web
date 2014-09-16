(function($) {
	
	'use strict';
	
	var about = {
		init : function() {
			this.addEventListener();
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
					okBtn : 'hide'
				});
			});
			$('#donations').click(function() {
				var body = 
					$('<div>'+
							'	<div class="text-center">'+
							'		<img class="img-responsive" src="'+window.baseUrl+'/images/alipay.jpg" style="max-width: 200px; display: inline; margin-bottom: 15px;">'+
							'	</div>'+
							'	<p>如果您确实觉得东皇钟帮助到了您，并且<b>愿意捐助任意金额</b>以支持鼓励作者继续保持更新。那么您可以打开“手机版支付宝客户端”，使用“扫一扫”功能扫描上面的二维码进行捐助。谢谢！</p>'+
							'   <p>如果您没有手机版支付宝客户端，也可以登陆<a href="http://www.alipay.com" target="_blank">支付宝</a>进行捐助，我的支付宝账号是：huxiao.mail@qq.com</p>'+
					'</div>');
				Modal.open({
					title : '捐助大嘴',
					body : body,
					backdrop : 'static',
					okBtn : 'hide'
				});
			});
		}
	};
	$(function() {
		about.init();
	});
	
})(jQuery);