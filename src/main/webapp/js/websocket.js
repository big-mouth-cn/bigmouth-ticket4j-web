(function($) {

	'use strict';

	var socket = new WebSocket(window.websocket);

	$(function() {
		socket.onopen = function(event) {
			$.notifier.success('WebSocket服务初始化成功，已连接到 ' + window.websocket);
			
			socket.onmessage = function(event) {
				console.log(event.data);
			};
		};
		
		socket.onerror = function(event) {
			$.notifier.error('系统初始化失败，无法连接：' + window.websocket);
		};
	});
})(jQuery);