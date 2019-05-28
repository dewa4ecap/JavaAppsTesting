<%@ page import="java.io.*,java.net.*,com.fe.scheduler.SchedulerEngine,org.apache.commons.io.FileUtils" %>
<%

URL r=SchedulerEngine.class.getResource("codehelp/"+request.getQueryString()+".html");
String content="<div>Error code "+request.getQueryString()+" not found</div>";
String title="";
if(r!=null){
	content=FileUtils.readFileToString(new File(r.getPath()));
}
if(request.getQueryString()==null){
	URL fs=SchedulerEngine.class.getResource("codehelp");
	if(fs!=null){
		File folder=new File(fs.getPath());
		File[] files=folder.listFiles();
		content="<div style='border:1px solid #b0b0b0;background:#e0e0e0;padding:20px;'><ul style='list-style:none;padding-left:3px;'>";
		for(int i=0;i<files.length;i++){
			content+="<li><a href='error_code.jsp?"+files[i].getName().replaceAll(".html", "")+"'>"+files[i].getName().replaceAll(".html", "")+"</a></li>";
		}
		content+="</ul></div>";
	}
}
%>
<html>
<head>
	<title><%=title%></title>
	<style>
	body, p { font-family:Tahoma; font-size:10pt; padding-left:30; }
	pre { font-size:8pt; }
	</style>
</head>
<body>

<%=content%>
</body>
</html>