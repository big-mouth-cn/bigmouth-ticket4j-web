var Global = {
	Msg: {
		DeleteConfirm: '您确定要删除这条项目吗?',
		InvaildRequest : '非法的请求.'
	},
	init : function() {
		//this.appendLoading();
	},
	
	appendLoading : function() {
		$('body').append($('<div class="loading navbar navbar-fixed-top"><div class="progress progress-striped active" style="height: 5px;"><div class="progress-bar" role="progressbar" aria-valuenow="45" aria-valuemin="0" aria-valuemax="100" style="width: 5%"><span class="sr-only"></span></div></div></div>'));
	},
	
	addFavorite : function() {
		try {
			window.external.addFavorite(window.location, document.title);
		} catch (e) {
			try {
				window.sidebar.addPanel(window.location, document.title, "");
			} catch (e) {
				alert("加入收藏失败，请使用Ctrl+D进行添加");
			}
		}
	}
};

var Ajax = {
	post: function (url, param, success, error) {
		this.ajax('post', url, param, success, error);
	},
	get: function (url, param, success, error) {
		this.ajax('get', url, param, success, error);
	},
	ajax: function (type, url, param, success, error) {
		$.ajax({
			url : url,
			data : param,
			dataType : 'json',
			cache : false,
			type : type,
			success : success,
			error : function(jqXHR, textStatus, errorThrown) {
				$.notifier.error('稍等片刻，系统繁忙，可能正在处理其他业务……');
				if (!!error) {
					error(jqXHR, textStatus, errorThrown);
				}
			}
		});
	}
};

var Tags = {
	val: function(regs) {
		return $(regs).val();
	},
	text: function(regs) {
		return $(regs).text();
	},
	focus: function(regs) {
		$(regs).focus();
	},
	disabled : function(regs, text) {
		$(regs).text(text);
		$(regs).attr('disabled', true);
	},
	enabled : function(regs, text) {
		$(regs).text(text);
		$(regs).attr('disabled', false);
	}
};

var Alert = {
	info: function(text, fn) {
		Dialog.show(text, null, fn);
	},
	success: function(text, fn) {
		Dialog.success(text, null, fn);
	},
	warn: function(text, fn) {
		Dialog.show(text, null, fn);
	},
	error: function(text, fn) {
		Dialog.error(text, null, fn);
	},
	confirm: function(text, fn) {
		Dialog.confirm(text, null, function() {
			if (!!fn) fn();
		});
	},
	/**
	 * options {
	 * 	  content(String),
	 *    title(String),
	 *    clickHandler(Function),
	 *    cancelHandler(Function),
	 *    clickText(String),
	 *    cancelText(String)
	 * }
	 */
	customConfirm : function(options) {
		Dialog.customConfirm(options);
	}
};

var Response = {
	ok: function(json) {
		if (json) {
			return json.Flag == 0;
		}
		else {
			return false;
		}
	}
};

var NumericUtils = {
	toFixed: function(value, len) {
		if (!len)
			len = 3;
		var result = parseFloat(value).toFixed(len);
		return parseFloat(result);
	},
	toMBytes: function(value) {
		return parseFloat(value) / 1024 / 1024;
	},
	toMBytesFixed: function(value, len) {
		return this.toFixed(this.toMBytes(value), len);
	},
	toDynamicBytesFixed: function(value, len) {
		if (value / 1024/1024/1024/1024 > 1) {
			return this.toFixed(value / 1024/1024/1024/1024, len) + ' <em>TB</em>';
		}
		else if (value / 1024/1024/1024 > 1) {
			return this.toFixed(value / 1024/1024/1024, len) + ' <em>GB</em>';
		}
		else if (value / 1024/1024 > 1) {
			return this.toFixed(value / 1024/1024, len) + ' <em>MB</em>';
		}
		else if (value / 1024 > 1) {
			return this.toFixed(value / 1024, len) + ' <em>KB</em>';
		}
		return value + ' <em>B</em>';
	},
	toDynamicNumericFixed: function(value, len) {
		if (value / 10000 > 1) {
			return this.toFixed(value/10000, len) + ' <em>万</em>';
		}
		return value;
	}
};

var StringUtils = {
	isBlank: function(string) {
		return !string || string.length <= 0;
	},
	isNotBlank: function(string) {
		return !this.isBlank(string);
	}
};

var CollectionUtils = {
	isEmpty: function(items) {
		return !items || items.length <= 0;
	},
	isNotEmpty: function(items) {
		return !this.isEmpty(items);
	}
};

var DateUtils = {
	toUTC_8 : function(time) {
		return time + (8 * 60 * 60 * 1000);
	},
	
	toMinute : function(secondes) {
		var minute = (parseFloat(secondes) / 1000 / 60);
		return minute >= 1 ? Math.round(minute) : 1;
	},
	
	format : function(date) {
		return date.getFullYear() 
			+ '-' + ( (date.getMonth() + 1) < 10 ? '0'+ (date.getMonth() + 1) : (date.getMonth() + 1)) 
			+ '-' + ( date.getDate() < 10 ? '0' + date.getDate() : date.getDate() ) 
			+ ' ' + ( date.getHours() < 10 ? '0' + date.getHours() : date.getHours() ) 
			+ ':' + ( date.getMinutes() < 10 ? '0' + date.getMinutes() : date.getMinutes());
	},
	getLastMonths : function(diff) {
		var date = new Date();
		date.setMonth(date.getMonth() - 1);
		return this.format(date);
	},
	getLastDays : function(value, diff) {
		var date = new Date();
		date.setTime(value.getTime());
		var last = date.getTime() - (diff*24*60*60*1000);
		date.setTime(last);
		return this.format(date);
	},
	getYesterday : function() {
		var date = new Date();
	 	var ye = date.getTime() - (24*60*60*1000);
	 	date.setTime(ye);
	 	return this.format(date);
	},
	getCurrent : function() {
		return this.format(new Date());
	},
	
	getRemind: function() {
		var date = new Date();
		var hour = date.getHours();
		if (hour > 5 && hour < 8)
			return '早上好！来一份营养早餐吧。';
		else if (hour < 12)
			return '上午好！';
		else if (hour < 14)
			return '中午好，忙碌了一个上午累了吧。休息休息，养足精神。';
		else if (hour < 18)
			return '下午好！';
		else if (hour < 21)
			return '晚上好！';
		else if (hour < 23)
			return '快到睡觉的时间咯！';
		else if (hour > 23 || hour < 5)
			return '夜深了，怎么还不休息呢？';
		else
			return '';
	}
};

Date.prototype.Format = function(fmt) {
	var o = {
		"M+" : this.getMonth() + 1, // 月份
		"d+" : this.getDate(), // 日
		"h+" : this.getHours(), // 小时
		"m+" : this.getMinutes(), // 分
		"s+" : this.getSeconds(), // 秒
		"q+" : Math.floor((this.getMonth() + 3) / 3), // 季度
		"S" : this.getMilliseconds()
	// 毫秒
	};
	if (/(y+)/.test(fmt))
		fmt = fmt.replace(RegExp.$1, (this.getFullYear() + "")
				.substr(4 - RegExp.$1.length));
	for ( var k in o)
		if (new RegExp("(" + k + ")").test(fmt))
			fmt = fmt.replace(RegExp.$1, (RegExp.$1.length == 1) ? (o[k])
					: (("00" + o[k]).substr(("" + o[k]).length)));
	return fmt;
};

var Page = {
	jump: function(pageNo) {
		jQuery("#pageNo").val(pageNo);
		jQuery("#mainForm").submit();
	}
};

var Table = {
	init: function() {
		this.addEventListener();
		this.short();
	},
	addEventListener: function() {
		
		$('input.search-input').each(function() {
			$(this).keydown(function(event) {
				if(event.keyCode==13) Table.search();
			});
		});
		
		// this.hover();
	},
	
	hover: function() {
		$('table tr').each(function() {
			var tr = $(this);
			var hoverIn = function() {
				tr.find('div.operation').fadeIn(300);
			};
			var hoverOut = function() {
				tr.find('div.operation').hide();
			};
			tr.hover(hoverIn, hoverOut);
		});
	},
	
	// 指定TD.short的单元格，将会对显示的文字进行截断，并设置title为完整值。
	short: function() {
		var tds = $("td.short");
		tds.each(function() {
			var td = $(this);
			var len = td.attr("maxlength");
			var text = td.text();
			if (text.length > parseInt(len)) {
				var span = $("<abbr>"+ text.substring(0, len) + "...</abbr>");
				span.attr("title", text);
				td.html(span);
			}
		});
	},
	search: function() {//搜索后显示第一页
		jQuery("#pageNo").val("1");
		jQuery("#mainForm").submit();
	},
	
	edit: function(primary, uri) {
		location.href = window.baseUrl + uri + primary;
	},
	
	// remove
	remove: function(primary, uri) {
		Alert.confirm(Global.Msg.DeleteConfirm, function() {
			location.href = window.baseUrl + uri + primary;
		});
	},
	
	removeAsyn: function(primary, url, success) {
		Alert.confirm(Global.Msg.DeleteConfirm, function() {
			var param = { id : primary };
			Ajax.get(url, param, success);
		});
	}
};

var Dialog = {
	width: 480,
	
	buildDiv : function() {
		var dialog = document.getElementById("dialog");
		if (dialog == undefined) 
		{
			$("body").append("<div id='dialog'></div>");
		}
	},
	buildDialog : function(title, buttons) {
		$("#dialog").dialog({
			width: Dialog.width,
			title: StringUtils.isBlank(title) ? '系统提示' : title,
			resizable: false,
			modal: true,
			open: function(event, ui) {
				Dialog.enabledMouseWheel(false);
			},
			close: function(event, ui) {
				Dialog.enabledMouseWheel(true);
			},
			buttons: buttons
		});
		this.settingContentPadding();
	},
	settingHtml: function(className, content) {
		Dialog.buildDiv();
		$("#dialog").html('');
		$("#dialog").html("<div class='" + className + "'><div class='content'>" + content + "</div></div>");
	},
	getOk: function(fn) {
		var buttons = 
			[
			    {
			    	text : "确定",
			    	class: 'btn btn-primary btn-sm',
			    	click : function() {
			    		if (!!fn) fn();
			    		$(this).dialog("close");
			    	}
			    }
			];
		return buttons;
	},
	show : function(content, title, fn) {
		this.settingHtml('dialog-message', content);
		var buttons = this.getOk(fn);
		this.buildDialog(title, buttons);
	},
	success: function(content, title, fn) {
		this.settingHtml('dialog-success', content);
		var buttons = this.getOk(fn);
		this.buildDialog(title, buttons);
	},
	error: function(content, title, fn) {
		this.settingHtml('dialog-error', content);
		var buttons = this.getOk(fn);
		this.buildDialog(title, buttons);
	},
	confirm : function(content, title, clickHandler, cancelHandler) {
		this.settingHtml('dialog-confirm', content);
		if (StringUtils.isBlank(title)) 
			title = "系统询问";
		var buttons = 
		[
			{
				text: "确定",
				class: 'btn btn-primary btn-sm',
				click: function() {
					clickHandler();
					$(this).dialog("close");
				}
			},
			{
				text: "取消",
				class: 'btn btn-default btn-sm',
				click: function() {
					if (cancelHandler) {
						cancelHandler();
					}
					$( this ).dialog( "close" );
				}
			}
		];
		this.buildDialog(title, buttons);
	},
	
	customConfirm : function(options) {
		this.settingHtml((!options.class) ? 'dialog-confirm' : options.class, options.content);
		if (StringUtils.isBlank(options.title)) 
			title = "系统询问";
		var buttons = 
		[
			{
				text: StringUtils.isBlank(options.clickText) ? "确定" : options.clickText,
				class: 'btn btn-primary btn-sm',
				click: function() {
					options.clickHandler();
					$(this).dialog("close");
				}
			},
			{
				text: StringUtils.isBlank(options.cancelText) ? "取消" : options.cancelText,
				class: 'btn btn-default btn-sm',
				click: function() {
					if (options.cancelHandler) {
						options.cancelHandler();
					}
					$( this ).dialog( "close" );
				}
			}
		];
		this.buildDialog(title, buttons);
	},
	settingContentPadding: function() {
		var outerHeight = $('#dialog').find('div.content').outerHeight();
		if (outerHeight < 71)
			return;
		$('#dialog').find('div.content').css({
			'padding-top' : 15,
			'line-height' : 1.428571429
		});
	},
	enabledMouseWheel : function(flag) {
		$('body').attr('onmousewheel', 'return '+flag+';');
	}
};

var PJax = {
	init : function() {
		//$(document).pjax('a', '#container');
	}
};

var Menu = {
	active : function(index) {
		$('#menu li').each(function() {
			$(this).removeClass('active');
			if ($(this).attr('index') == index)
				$(this).addClass('active');
		});
	}
};

var Cookie = {
	COOKIE : 'COOKIE',
	attr: {
	},
	init: function() {
		$.cookie.json = true;
		var cookie = $.cookie(Cookie.COOKIE);
		if (!cookie) {
			Cookie.written({});
		}
		else {
			this.attr = cookie;
		}
	},
	write: function(obj) {
		if (obj) {
			for (var key in obj) {
				var value = obj[key];
				Cookie.attr[key] = value;
			}
			Cookie.written(Cookie.attr);
		}
	},
	written: function(obj) {
		$.cookie(Cookie.COOKIE, obj, { expires : 365 * 50, path : '/' });
	},
	read: function() {
		return Cookie.attr;
	}
};

var QQ = {
	login: function(url) {
		var width = 760;
		var height = 510;
		window.open(url, '', 'toolbar=no, width='+width+', height='+height+',top='+(screen.height-height)/2+',left='+(screen.width-width)/2);
	}
};

var Modal = {
	attr: {
		id : 'modal',
		label : 'modalLabel'
	},
	tags: {
		modal: null, 
		modal_dialog: null, 
		modal_content: null, 
		modal_header: null, 
		modal_header_close: null, 
		modal_header_title: null, 
		modal_body: null, 
		modal_footer: null, 
		modal_footer_close: null, 
		modal_footer_ok: null
	},
	init: function() {
		this.build();
	},
	
	modal: function() {
		return $('#' + this.attr.id);
	},
	
	/**
	 * options: {
	 *     title: String
	 *     body: Document
	 *     backdrop: Boolean
	 *     shown: function
	 *     hiden: function
	 *     closeHandler: function
	 *     okHandler: function
	 *     size: lg|sm,
	 *     okBtn: show|hide,
	 *     closeBtn: show|hide
	 * }
	 */
	open: function(options) {
		this.empty();
		
		if (options) {
			var title = options.title;
			var body = options.body;
			var close = options.closeHandler;
			var ok = options.okHandler;
			var size = options.size;
			
			if (title) {
				this.tags.modal_header_title.text(title);
			}
			if (body) {
				this.tags.modal_body.append(body);
			}
			if (close) {
				this.tags.modal_footer_close.bind('click', close);
			}
			if (ok) {
				this.tags.modal_footer_ok.bind('click', ok);
			}
			if (size) {
				this.tags.modal_dialog.addClass((size == 'lg') ? 'modal-lg' : 'modal-sm');
			}
			
			this.modal().bind('shown.bs.modal', function() {
				if (options.shown) options.shown();
			});
			this.modal().bind('hidden.bs.modal', function() {
				if (options.hiden) options.hiden();
			});
			if (options.backdrop)
				this.modal().data('backdrop', options.backdrop);
		}
		
		this.modal().modal();
		this.tags.modal_footer_ok.removeClass('hide');
		this.tags.modal_footer_close.removeClass('hide');
		if (options) {
			if (options.okBtn && options.okBtn == 'hide') {
				this.hideOkButton();
			}
			if (options.closeBtn && options.closeBtn == 'hide') {
				this.hideCloseButton();
			}
		}
	},
	
	close: function() {
		this.modal().modal('hide');
	},
	
	empty: function() {
		this.tags.modal_header_title.text('');
		this.tags.modal_body.empty();
		this.tags.modal_footer_close.unbind('click');
		this.tags.modal_footer_ok.unbind('click');
		this.modal().unbind('shown.bs.modal');
		this.modal().unbind('hidden.bs.modal');
		this.tags.modal_dialog.removeClass('modal-lg modal-sm');
	},
	
	hideOkButton: function() {
		this.tags.modal_footer_ok.addClass('hide');
	},
	hideCloseButton: function() {
		this.tags.modal_footer_close.addClass('hide');
	},
	
	build: function() {
		if (this.modal().length > 0) {
			return;
		}
		
		this.tags.modal = $('<div class="modal fade" id="' + this.attr.id + '" tabindex="-1" role="dialog" aria-labelledby="' + this.attr.label + '" aria-hidden="false" data-backdrop="false">');
		this.tags.modal_dialog = $('<div class="modal-dialog">');
		this.tags.modal_content = $('<div class="modal-content">');
		this.tags.modal_header = $('<div class="modal-header">');
		this.tags.modal_header_close = $('<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>');
		this.tags.modal_header_title = $('<h4 class="modal-title" id="' + this.attr.label + '"></h4>');
		this.tags.modal_body = $('<div class="modal-body">');
		this.tags.modal_footer = $('<div class="modal-footer">');
		this.tags.modal_footer_close = $('<button type="button" class="btn btn-default" data-dismiss="modal">关闭</button>');
		this.tags.modal_footer_ok = $('<button type="submit" class="btn btn-primary">确定</button>');
		
		this.tags.modal_footer.append(this.tags.modal_footer_close);
		this.tags.modal_footer.append(this.tags.modal_footer_ok);
		
		this.tags.modal_header.append(this.tags.modal_header_close);
		this.tags.modal_header.append(this.tags.modal_header_title);
		
		this.tags.modal_content.append(this.tags.modal_header);
		this.tags.modal_content.append(this.tags.modal_body);
		this.tags.modal_content.append(this.tags.modal_footer);
		
		this.tags.modal_dialog.append(this.tags.modal_content);
		
		this.tags.modal.append(this.tags.modal_dialog);
		
		$('body').append(this.tags.modal);
	}
};

jQuery(function() {
	PJax.init();
	Global.init();
	Table.init();
	Cookie.init();
	Modal.init();
});