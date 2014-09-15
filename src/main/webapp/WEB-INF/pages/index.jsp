<%@ page contentType="text/html; charset=UTF-8" language="java"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page contentType="text/html; charset=UTF-8" language="java"%>
<% request.setCharacterEncoding("UTF-8"); %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html lang="zh-cn">
<head>
	<title>东皇钟</title>
	<meta charset="utf-8">
	<meta http-equiv="X-UA-Compatible" content="IE=edge">
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	
	<link rel="shortcut icon" href="${ctx }/images/favicon.png">

	<link rel="stylesheet" href="http://cdn.bootcss.com/bootstrap/3.2.0/css/bootstrap.min.css">
<!-- 	<link rel="stylesheet" href="http://cdn.bootcss.com/bootstrap/3.2.0/css/bootstrap-theme.min.css"> -->
	<link href="${ctx }/css/sticky-footer-navbar.css" rel="stylesheet">
	<link href="${ctx }/css/bootstrap-datetimepicker.min.css" rel="stylesheet">
	<link href="${ctx }/css/big-mouth.common.css" rel="stylesheet">
	<link href="${ctx }/js/notifier/allen-notifier.default.css" rel="stylesheet">
	<link href="${ctx }/js/autocomplete/jquery.autocomplete.css" rel="stylesheet">
	<script>window.baseUrl = "${ctx}"; window.websocket = '${webSocketAddr}'</script>
	<script src="http://cdn.bootcss.com/jquery/1.11.1/jquery.min.js"></script>
	<script src="http://cdn.bootcss.com/bootstrap/3.2.0/js/bootstrap.min.js"></script>
	<script src="${ctx }/js/datetimepicker/bootstrap-datetimepicker.min.js"></script>
	<script src="${ctx }/js/datetimepicker/locales/bootstrap-datetimepicker.zh-CN.js"></script>
	<script src="${ctx }/js/autocomplete/jquery.autocomplete.min.js"></script>
	<script src="${ctx }/js/notifier/allen-notifier.js"></script>
	<script src="${ctx }/js/jquery.sortable.js"></script>
	<script src="${ctx }/js/jquery.cookie.js"></script>
	<script src="${ctx }/js/jquery.pjax.js"></script>
	<script src="${ctx }/js/json2.js"></script>
	<script src="${ctx }/js/big-mouth.common.js"></script>
	<script src="${ctx }/js/websocket.js"></script>
	
	<style type="text/css">
	.center {text-align: center;}
	#loginDialog .modal-body {padding: 20px;}
	.list, .simple-list, .users {list-style: none; margin: 0; padding: 0;}
	.list.move li {cursor: move;}
	.list li {
		line-height: 30px;
		float: left;
		width: 80px;
		height: 30px;
		text-align: center;
		border: 1px solid #ccc;
		border-radius: 4px;
		padding: 0px;
		margin: 5px 0px;
		margin-right: 10px;
		position: relative;
	}
	.list li a.remove {
		position: absolute;top:0;right: 0;
		text-decoration: none;
		color: #333;
	}
	.list li a.remove:HOVER {color: #c12e2a;}
	
	.autohide a {display: none;}
	.autohide:HOVER a {display: block;}
	
	.users li {float: left; margin-right: 10px;}
	.users li.users-internal {margin-top: 5px; margin-bottom: 5px;}
	.users li.item {
		display: inline-block;
		padding: 6px 12px;
		margin-bottom: 0;
		font-size: 14px;
		font-weight: 400;
		line-height: 1.42857143;
		text-align: center;
		white-space: nowrap;
		vertical-align: middle;
		cursor: pointer;
		-webkit-user-select: none;
		-moz-user-select: none;
		-ms-user-select: none;
		user-select: none;
		background-image: none;
		border: 1px solid transparent;
		border-radius: 4px;
		cursor: default;
		color: #333;
		background-color: #fff;
		border-color: #ccc;
	}
	a.glyphicon:LINK {
		color: #333;
		text-decoration: none;
	}
	
	.loading {height: 105px;background: url('images/loading.gif') no-repeat center top;}
	.loading .span {text-align: center;padding-top: 65px;}
	</style>
</head>
<body>
	<nav class="navbar navbar-inverse navbar-fixed-top" role="navigation">
      <div class="container">
        <div class="navbar-header">
          <button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#navbar" aria-expanded="false" aria-controls="navbar">
            <span class="sr-only">Toggle navigation</span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
          </button>
          <a class="navbar-brand" href="#">东皇钟</a>
        </div>
        <div id="navbar" class="navbar-collapse collapse">
          <ul class="nav navbar-nav">
            <li class="active"><a href="#">订票</a></li>
            <li class="dropdown">
            	<a href="#" class="dropdown-toggle" data-toggle="dropdown">待支付订单<b class="caret"></b></a>
            	<ul id="noCompleteList" class="dropdown-menu">
            		<li class="dropdown-header">请选择账号</li>
            	</ul>
            </li>
            <li><a href="#settings">系统设置</a></li>
            <li><a href="#about">关于</a></li>
            <li><a href="#donations">捐助</a></li>
          </ul>
        </div>
      </div>
    </nav>
    
    <section class="container">
    	<div class="panel panel-default">
   			<div class="panel-body">
   				<ul id="users" class="users">
   					<li><button id="btnAddSession" class="btn btn-primary">账号登陆</button></li>
   				</ul>
   			</div>
    	</div>
    	
    	<div class="panel panel-default">
    		<div class="panel-heading">订单处理</div>
   			<table id="tblTickets" class="table">
   				<thead>
   					<tr>
   						<th width="120">乘车日期</th>
   						<th width="100">出发站</th>
   						<th width="100">到达站</th>
   						<th width="100">指定车次</th>
   						<th width="100">黑名单</th>
   						<th width="100">席别</th>
   						<th width="100">乘车人</th>
   						<th>状态</th>
   						<th width="50"></th>
   					</tr>
   				</thead>
   				<tbody>
   					<tr class="empty">
   						<td colspan="9"><div class="alert alert-info" style="margin-bottom: 0px;">暂时没有正在处理的订单，您可以单击“添加订单”开始进行购票。</div></td>
   					</tr>
   				</tbody>
   			</table>
    		<div class="panel-footer">
    			<button id="btnAddTicket" class="btn btn-primary" data-toggle="popover" data-trigger="focus">添加订单</button>
    		</div>
    	</div>
    </section>
    
    <div class="footer">
      <div class="container">
        <p class="text-muted">&copy; 2014 <a href="http://www.big-mouth.cn" target="_blank">big-mouth.cn</a></p>
      </div>
    </div>
    
</body>
</html>
<script src="https://kyfw.12306.cn/otn/resources/js/framework/station_name.js?station_version=1.809"></script>
<script src="${ctx }/js/session/session.js"></script>
<script src="${ctx }/js/ticket4j/order.js"></script>
<script src="${ctx }/js/ticket4j/noComplete.js"></script>