(function($) {
	
	'use strict';
	
	var Event = {
		INITIALIZE_COMPLETED : "initialize.completed", 
		PASSCODE_DOWNLOAD_COMPLETED : "passcode.download.completed",
		LOGIN_SUCCESSFUL : "login.successful",
		LOGIN_FAILED : "login.failed"
	}
	
	var ticket4jHttpResponse;
	
	var sessions = [];
	
	function Session(username) {
		this.username = username;
		this.password;
		this.passenger;
		this.response;
		this.ticket4jHttpResponse;
		this.signIn = false;
		
		this.setPassword = function(password) {
			this.password = password;
		};
		this.setPassenger = function(passenger) {
			this.passenger = passenger;
		};
		this.setResponse = function(response) {
			this.response = response;
		};
		this.setTicket4jHttpResponse = function(ticket4jHttpResponse) {
			this.ticket4jHttpResponse = ticket4jHttpResponse;
		};
		this.setSignIn = function(signIn) {
			this.signIn = signIn;
		};
		return this;
	}
	
	$.session = {
		getSessionByUsername : function(username) {
			if (CollectionUtils.isNotEmpty(sessions)) {
				for (var i = 0; i < sessions.length; i++) {
					if (sessions[i].username == username) 
						return sessions[i];
				}
			}
			return null;
		}
	};
	
	var internal = {
		init: function() {
			this.addEventListener();
			this.getSessions();
		},
		
		render : {
			append: function(session) {
				var $this = $('#'+ $.md5(session.username));
				if ($this.length == 0) {
					// Does not exist
					var span = $('<span>');
					span.append(session.username);
					var resign = $('<a href="javascript:;" class="resign" style="font-size:12px;">(重新登陆)</a>');
					resign.data('session', session);
					resign.bind('click', function() {
						internal.openDialog($(this).data('session'));
					});
					span.append(resign);
					if (session.signIn) {
						resign.hide();
					}
					var li = $('<li class="item">');
					li.append(span);
					li.attr('id', $.md5(session.username));
					$('#users').append(li);
				}
				else {
					if (session.signIn) {
						$this.find('.resign').hide();
					}
					else {
						$this.find('.resign').show();
					}
				}
			}
		},
		
		addEventListener: function() {
			$(window).bind('SESSION_EVENT', function(evt, data) {
				internal.append(data);
			});
			$('#btnAddSession').click(function() {
				internal.openDialog(null);
			});
		},
		
		openDialog : function(session) {
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
				'		<div class="col-md-5"><img src="#" style="height: 34px; cursor: pointer; display: none;"></div>'+
				'	</div>'+
				'  </div>'+
				'  <button id="btnLogin" type="button" class="btn btn-primary btn-lg btn-block" data-loading-text="正在登陆...">登陆</button>'+
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
						if (session) {
							$loginName.val(session.username);
							$password.val(session.password);
						}
						if (!session) {
							$loginName.blur(function() {
								$(sessions).each(function(i, item) {
									if (item.username == $loginName.val()) {
										$.notifier.info($loginName.val() + ' 已经登陆，请登陆其他账户。');
										$loginName.val('');
										$loginName.focus();
										return;
									}
								});
							});
						}
						$btnLogin.click(function() {
							if (StringUtils.isBlank($loginName.val())) {
								$(loginName).focus(); return;
							}
							if (StringUtils.isBlank($password.val())) {
								$(password).focus(); return;
							}
							if (StringUtils.isBlank($loginPassCode.val())) {
								$(loginPassCode).focus(); return;
							}
							$btnLogin.button('loading');
							internal.login($loginName.val(), $password.val(), $loginPassCode.val());
						});
						
						var iptKeydown = function(compoments) {
							$(compoments).each(function() {
								$(this).keydown(function(evt) {
									if (evt.keyCode == 13)
										$btnLogin.trigger('click');
								});
							});
						};
						iptKeydown([ $loginName, $password, $loginPassCode ]);
						
						body.find('img').click(internal.loadLoginPassCode);
						
						$(window).bind(Event.INITIALIZE_COMPLETED, function() {
							internal.loadLoginPassCode();
						});
						$(window).bind(Event.PASSCODE_DOWNLOAD_COMPLETED, function(evt, data) {
							$(loginPassCode).val(data.rec);
							body.find('img').attr('src', data.path).fadeIn();
						});
						$(window).bind(Event.LOGIN_SUCCESSFUL, function(evt, data) {
							$.notifier.success("登陆成功，立即添加订单开始购票吧。");
							Modal.close();
							
							var session = new Session($loginName.val());
							session.setPassword(data.password);
							session.setResponse(data.response);
							session.setPassenger(data.passengers);
							session.setTicket4jHttpResponse(ticket4jHttpResponse);
							session.setSignIn(data.signIn);
							internal.append(session);
						});
						$(window).bind(Event.LOGIN_FAILED, function(evt) {
							$btnLogin.button('reset');
						});
						internal.initCookie();
						body.find('#loginName').focus();
					},
					hiden: function() {
						$(window).unbind(Event.INITIALIZE_COMPLETED);
						$(window).unbind(Event.PASSCODE_DOWNLOAD_COMPLETED);
						$(window).unbind(Event.LOGIN_SUCCESSFUL);
						$(window).unbind(Event.LOGIN_FAILED);
					}
				});
		},
		
		append : function(session) {
			var contains = false;
			for (var i = 0; i < sessions.length; i++) {
				var item = sessions[i];
				if (item.username == session.username) {
					item = session;
					contains = true;
					break;
				}
			}
			if (!contains)
				sessions.push(session);
			internal.render.append(session);
			$(window).trigger('sessions.change', [ sessions ]);
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
					$(window).trigger(Event.LOGIN_FAILED);
					$.notifier.error(json.Message);
				}
			});
		},
		
		getSessions: function() {
			Ajax.get(window.baseUrl + '/session!getSessions.shtml', {}, function(json) {
				if (Response.ok(json)) {
					var data = json.Data;
					$(data).each(function(i, item) {
						var session = new Session(item.username);
						session.setPassword(item.password);
						session.setResponse(item.response);
						session.setPassenger(item.passengers);
						session.setTicket4jHttpResponse(item.ticket4jHttpResponse);
						session.setSignIn(item.signIn);
						internal.append(session);
					});
				}
				else {
					$.notifier.error(json.Message);
				}
			});
		}
	}
	
	$(function() {
		internal.init();
	});
})(jQuery);