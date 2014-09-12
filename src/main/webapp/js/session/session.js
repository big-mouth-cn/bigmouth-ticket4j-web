(function($) {
	
	'use strict';
	
	var Event = {
		INITIALIZE_COMPLETED : "initialize.completed", 
		PASSCODE_DOWNLOAD_COMPLETED : "passcode.download.completed",
		LOGIN_SUCCESSFUL : "login.successful"
	}
	
	var ticket4jHttpResponse;
	
	var internal = {
		addEventListener: function() {
			$('#btnAddSession').click(function() {
				var body = 
				$('<form role="form">'+
				'  <div class="form-group">'+
				'	<label for="loginName">登录名</label>'+
				'	<input class="form-control" id="loginName" placeholder="用户名/邮箱">'+
				'  </div>'+
				'  <div class="form-group">'+
				'	<label for="password">密码</label>'+
				'	<input type="password" class="form-control" id="password" placeholder="">'+
				'  </div>'+
				'  <div class="form-group">'+
				'	<label for="loginPassCode">验证码</label>'+
				'	<div class="row">'+
				'		<div class="col-md-5">'+
				'			<input class="form-control" id="loginPassCode" maxlength="4">'+
				'		</div>'+
				'		<div class="col-md-5"><img src="#" style="height: 34px;"></div>'+
				'	</div>'+
				'  </div>'+
				'  <div class="checkbox">'+
				'	<label>'+
				'	  <input type="checkbox"> 记住我'+
				'	</label>'+
				'  </div>'+
				'  <button id="btnLogin" type="button" class="btn btn-primary btn-lg btn-block">登陆</button>'+
				'</form>');
				Modal.open({
					title : '用户登陆',
					body : body,
					backdrop : 'static',
					size : 'sm',
					okBtn : 'hide',
					closeBtn : 'hide',
					shown : function() {
						var $btnLogin = body.find('#btnLogin');
						var $loginName = body.find('#loginName');
						var $password = body.find('#password');
						var $loginPassCode = body.find('#loginPassCode');
						$(window).bind(Event.INITIALIZE_COMPLETED, function() {
							internal.loadLoginPassCode();
						});
						$(window).bind(Event.PASSCODE_DOWNLOAD_COMPLETED, function(evt, data) {
							body.find('img').attr('src', data);
							
							$btnLogin.click(function() {
								var loginName = $loginName.val();
								var passwd = $password.val();
								var passCode = $loginPassCode.val();
								internal.login(loginName, passwd, passCode);
							});
						});
						$(window).bind(Event.LOGIN_SUCCESSFUL, function(evt, data) {
							$.notifier.success('OK');
						});
						internal.initCookie();
						body.find('#loginName').focus();
					},
					hiden: function() {
						$(window).unbind(Event.INITIALIZE_COMPLETED);
						$(window).unbind(Event.PASSCODE_DOWNLOAD_COMPLETED);
						$(window).unbind(Event.LOGIN_SUCCESSFUL);
					}
				});
			});
		},
		
		initCookie: function() {
			Ajax.get(window.baseUrl + '/session!initialize.shtml', {}, function(json) {
				if (Response.ok(json)) {
					ticket4jHttpResponse = json.Data;
					$(window).trigger(Event.INITIALIZE_COMPLETED);
				}
				else {
					$.notifier.error(json.Message);
				}
			});
		},
		
		loadLoginPassCode: function() {
			Ajax.post(window.baseUrl + '/session!getLoginPassCode.shtml', {
				ticket4jHttpResponse : JSON.stringify(ticket4jHttpResponse)
			}, function(json) {
				if (Response.ok(json)) {
					$(window).trigger(Event.PASSCODE_DOWNLOAD_COMPLETED, [ json.Data ]);
				}
				else {
					$.notifier.error(json.Message);
				}
			});
		},
		
		login: function(username, passwd, passCode) {
			Ajax.post(window.baseUrl + '/session!login.shtml', {
				'username' : username,
				'passwd' : passwd,
				'loginPassCode' : passCode,
				'ticket4jHttpResponse' : JSON.stringify(ticket4jHttpResponse)
			}, function(json) {
				if (Response.ok(json)) {
					$(window).trigger(Event.LOGIN_SUCCESSFUL, [ json.Data ]);
				}
				else {
					$.notifier.error(json.Message);
				}
			});
		}
	}
	
	$(function() {
		internal.addEventListener();
	});
})(jQuery);