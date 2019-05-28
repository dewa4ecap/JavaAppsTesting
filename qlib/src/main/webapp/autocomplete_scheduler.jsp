<%@page import="com.fourelementscapital.config.Constant"%>
<%@page import="com.fourelementscapital.qlib.common.JspXHRRequest"%>
<%@ page contentType="application/json; charset=utf-8" language="java" import="com.fourelementscapital.client.*" errorPage="" %><%

Cookie cookie = null;
Cookie[] cookies = null;
String choosenGroup = "";

cookies = request.getCookies();
if (cookies!=null) {
	for (int a=0;a<cookies.length;a++) {
		cookie = cookies[a];
		if(cookie.equals("group")) choosenGroup = cookie.getValue();
	}
}

if(request.getParameter("term")!=null){
	response.setContentType("text/plain");
	response.setHeader("Content-Disposition", "inline");
	String json="";
	if(request.getParameter("xhr_rfunction")!=null){
	  json=new JspXHRRequest(request).getXHRDataRSSO(choosenGroup);
	}
	
	if(request.getParameter("xhr_scheduler")!=null){
		 json=new JspXHRRequest(request).getXHRDataScheduler();
	}
	out.print(json);
	out.flush();	
}else if(request.getParameter("is_auth")!=null){ 
	String ky=(String)session.getAttribute(Constant.SESSION_LOGGED_USER);
	 
	response.setContentType("text/plain");
	response.setHeader("Content-Disposition", "inline");
	if(ky!=null && !ky.trim().equals("")){
		out.print("{\"auth\":true}");
	}else{
		out.print("{\"auth\":false}");
	}
	out.flush();
}else if(request.getParameter("notification")!=null){ 
	try{
		response.setContentType("application/json");
		response.setHeader("Content-Disposition", "inline");
		String json=new JspXHRRequest(request).getNotificationData();
		out.print(json);
		out.flush();	 		
	}catch(Exception e){
		response.setContentType("application/json");
		response.setHeader("Content-Disposition", "inline");
		out.print("{\"error\":true}");		
		out.flush();
	}
}else if(request.getParameter("get_notification_details")!=null){ 
	try{
		response.setContentType("application/json");
		response.setHeader("Content-Disposition", "inline");
		 
		
		long sc_id=0;
		String stat=request.getParameter("stat");
		try{		
			sc_id=Long.parseLong(request.getParameter("scheduler_id"));			
		}catch(Exception e){ }
		String json=new JspXHRRequest(request).listLast2DaysFailedDetails(sc_id, stat);
		out.print(json);
		out.flush();	 		
	}catch(Exception e){
		response.setContentType("application/json");
		response.setHeader("Content-Disposition", "inline");
		out.print("{\"error\":true}");		
		out.flush();
	}	
	
	
}else if(request.getParameter("get_execlogs")!=null){ 
	try{
		response.setContentType("application/json");
		response.setHeader("Content-Disposition", "inline");
		 
		
		int sc_id=0;
		long tri_time=0;
		try{		
			sc_id=Integer.parseInt(request.getParameter("scheduler_id"));			
			tri_time=Long.parseLong(request.getParameter("trigger_time"));			
		}catch(Exception e){ }
		String json=new JspXHRRequest(request).getExecutionLogs(sc_id, tri_time);
		out.print(json);
		out.flush();	 		
	}catch(Exception e){
		response.setContentType("application/json");
		response.setHeader("Content-Disposition", "inline");
		out.print("{\"error\":true}");		
		out.flush();
	}	
}else{
	try{
		response.setContentType("application/json");
		response.setHeader("Content-Disposition", "inline");
		String json=new JspXHRRequest(request).getJSON();
		out.print(json);
		out.flush();	 		
	}catch(Exception e){
		e.printStackTrace();
	}
} 
 
%>