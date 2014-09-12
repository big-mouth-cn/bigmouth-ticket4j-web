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
	<link rel="stylesheet" href="http://cdn.bootcss.com/bootstrap/3.2.0/css/bootstrap-theme.min.css">
	<link href="${ctx }/css/sticky-footer-navbar.css" rel="stylesheet">
	<link href="${ctx }/css/big-mouth.common.css" rel="stylesheet">
	<link href="${ctx }/js/notifier/allen-notifier.default.css" rel="stylesheet">
	
	<script src="http://cdn.bootcss.com/jquery/1.11.1/jquery.min.js"></script>
	<script src="http://cdn.bootcss.com/bootstrap/3.2.0/js/bootstrap.min.js"></script>
	<script src="${ctx }/js/notifier/allen-notifier.js"></script>
	<script src="${ctx }/js/jquery.cookie.js"></script>
	<script src="${ctx }/js/jquery.pjax.js"></script>
	<script src="${ctx }/js/json2.js"></script>
	<script src="${ctx }/js/big-mouth.common.js"></script>
	
	<script>window.baseUrl = "${ctx}";</script>
	
	<style type="text/css">
	#loginDialog .modal-body {padding: 20px;}
	</style>
</head>
<body>
	<nav class="navbar navbar-default navbar-fixed-top" role="navigation">
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
            <li class="active"><a href="#">自助订票</a></li>
            <li><a href="#contact">联系作者</a></li>
            <li><a href="#about">关于</a></li>
            <li><a href="#donations">捐助</a></li>
          </ul>
        </div>
      </div>
    </nav>
    
    <section class="container">
    	<button id="btnAddSession" class="btn btn-default">添加新会话</button>
    </section>
    
    <div class="footer">
      <div class="container">
        <p class="text-muted">&copy; 2014 <a href="http://www.big-mouth.cn" target="_blank">big-mouth.cn</a></p>
      </div>
    </div>
    
</body>
</html>
<script src="${ctx }/js/session/session.js"></script>