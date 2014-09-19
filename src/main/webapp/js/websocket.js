(function($) {

	'use strict';

	var socket = null,
		initialized = false,
		CHECK_TIME_MILLIS = 5000;

	$(function() {
		initSocket();
		setInterval(function() {
			if (!initialized) {
				initSocket();
			}
		}, CHECK_TIME_MILLIS);
	});
	
	function initSocket() {
		socket = new WebSocket(window.websocket);
		socket.onopen = function(event) {
			initialized = true;
		};
		socket.onmessage = function(event) {
			var data = event.data;
			if (typeof (data) != 'undefined') {
				var json = JSON.parse(data);
				var messageType = json.messageType;
				$(window).trigger(messageType, [ json.data ]);
			}
		};
		socket.onclose = function(evt) {
			initialized = false;
		};
		socket.onerror = function(event) {
			$.notifier.error('无法连接：' + window.websocket);
		};
	}
})(jQuery);