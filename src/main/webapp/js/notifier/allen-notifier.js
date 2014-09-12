(function($) {
	
	// Notifier
	
	'use strict';
	
	var options;
	var NotifierType = {
		INFO : 'info',
		SUCCESS : 'success',
		ERROR : 'error'
	};

	$.notifier = {
		defaults : {
			title : '',
			content : '',
			delay : 5000,
			autoClose : true,
			type : NotifierType.INFO
		},
		
		info : function(args) {
			var object = typeOf(this.defaults, args);
			object.type = NotifierType.INFO;
			this.show(object);
		},
		
		success : function(args) {
			var object = typeOf(this.defaults, args);
			object.type = NotifierType.SUCCESS;
			this.show(object);
		},
		
		error : function(args) {
			var object = typeOf(this.defaults, args);
			object.type = NotifierType.ERROR;
			this.show(object);
		},
		
		show : function(args) {
			options = $.extend(this.defaults, args);
			
			createIfNotExist();
			var container = getNotifier();
			
			var $ele = $('<li>' + (options.content || '') + '</li>');
			container.find('ul').append($ele);
			$ele.addClass('animateIn');
			
			if (options.autoClose) {
				setTimeout(function() {
					$ele.addClass('animateOut');
				}, options.delay);
			}
			$ele.addClass(options.type);
		},
		
		container : function() {
			return getNotifier();
		}
	}
	
	function typeOf(defaults, args) {
		var object = {};
		if (typeof args == 'string') {
			object.content = args;
		}
		else {
			object = $.extend(defaults, args);
		}
		return object;
	}
	
	function createIfNotExist() {
		if (!exist()) {
			var notifier = $('<div class="notifier"><ul></ul></div>');
			$('body').append(notifier);
		}
	}
	
	function exist() {
		var notifier = getNotifier();
		return (notifier.length > 0);
	}
	
	function getNotifier() {
		return $('div.notifier');
	}
	
	function init() {
		$(document).bind("webkitAnimationEnd oAnimationEnd msAnimationEnd animationend", function(e) {
			if (e.originalEvent.animationName === "slideUp") {
				$(e.target).remove();
			}
		});
	}
	
	init();
	
})(jQuery);