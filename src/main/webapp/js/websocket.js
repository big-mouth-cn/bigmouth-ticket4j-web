(function($) {

	'use strict';

	var socket = null,
		initialized = false;

	$(function() {
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
	});
})(jQuery);