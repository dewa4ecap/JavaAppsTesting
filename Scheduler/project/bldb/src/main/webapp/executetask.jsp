<%@ page import="java.util.*,org.quartz.JobExecutionException,com.fourelementscapital.db.*,com.fe.scheduler.*" %>

<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
<title>Task Excecution</title>

</head>
<body>
<%
	String message="";
	if(request.getParameter("id")==null || (request.getParameter("id")!=null && ((String)request.getParameter("id")).equals("")) ) {
			message="Job not found,Please provide the correct job";
	}else{
	      
		
		try{
			
				SchedulerDB sdb=SchedulerDB.getSchedulerDB();
				
    			sdb.connectDB();    			
    			
    		    int scheduler_id=Integer.parseInt(request.getParameter("id"));
    			Map data=sdb.getScheduler(scheduler_id);
    			if(data!=null){
	    			Number active=(Number)data.get("active");
	    			if(active!=null && active.intValue()==-1 ){
	    					throw new Exception("Inactive task can't be executed");
	    			}
	    			String taskuid=(String)data.get("taskuid");
	    			String name=(String)data.get("name");
					
                   
	    			//if(new SchedulerEngine().isJobScheduled(name,taskuid)){	             
	    				new SchedulerEngine().executeJobNow(name, taskuid,data,sdb);
		    			sdb.closeDB();		    		
		    	   		message="Job has been added into the queue";
					//}else{
					 //   throw new Exception("Task is not scheduled in this host");
					//}	
			   }else{
			      throw new Exception("Invalid ID");
			   }
    	
		}catch(Exception e){
			//ClientErrorMgmt.reportError(e, null);
			//throw e;
			message="Error occured: MSG:"+e.getMessage();
		}
	
  }
		
	
%>
<%=message%>
</body>
</html>