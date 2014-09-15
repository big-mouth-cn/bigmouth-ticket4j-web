(function($) {

	'use strict';

	var socket = new WebSocket(window.websocket);

	$(function() {
		socket.onopen = function(event) {
			socket.onmessage = function(event) {
				var data = event.data;
				if (typeof (data) != 'undefined') {
					var json = JSON.parse(data);
					var messageType = json.messageType;
					$(window).trigger(messageType, [ json.data ]);
				}
			};
		};
		
		socket.onerror = function(event) {
			$.notifier.error('无法连接：' + window.websocket);
		};
	});
})(jQuery);