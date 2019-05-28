<html>
	<head>
		<title>Scheduler API</title>
	</head>
	<body>
		
	<div align="center" style="border:1px solid grey; padding:3px; font-size:3em; background-color:grey">SchedulerAPI</div>	
		<P>

 
<h1>run</h1>

 Executes scheduled tasks
 <pre>
 <u>Parameters:</u>ids={id1,id2...}
 <u>Parameters:</u>[delay=minutes]
 <u>Returns:</u>  
 <code>&lt;?xml version="1.0"?&gt;
 &lt;result&gt;done&lt;/result&gt;
 </code>

 <u>Example:</u> http://server-ip-or-name/bldb/schedulerAPI?method=run&ids=21,22
 <u>Example:</u> http://server-ip-or-name/bldb/schedulerAPI?method=run&ids=21,22&delay=15
 
 <hr> 
 </pre>
 
 
 
 <h1>livepeers</h1>
 This method lists out all live peers 
 <pre>
 <u>Parameters:</u>Nil
 <u>Returns:</u>  
 <code>&lt;?xml version="1.0"?&gt;

 &lt;result&gt;
 	&lt;peer status="NOBUSY"&gt;4ECAPSVSG1&lt;/peer&gt;
 	&lt;peer status="NOBUSY"&gt;4ecappcsg11&lt;/peer&gt;
 	&lt;peer status="NOBUSY"&gt;4ecappcsg12&lt;/peer&gt;

 	&lt;peer status="BUSY"&gt;4ecappcsg14&lt;/peer&gt;
 	&lt;peer status="BUSY"&gt;4ecappcsg7&lt;/peer&gt;
 	&lt;peer status="BUSY"&gt;4ecapsvsg5&lt;/peer&gt;
 	&lt;peer status="BUSY"&gt;dev-server&lt;/peer&gt;

 &lt;/result&gt;
 </code>
 <u>Example:</u> http://server-ip-or-name/bldb/schedulerAPI?method=livepeers
 <hr> 
 </pre>
   
 
 <h1>queue</h1>
 This method lists all tasks that currently executing by peers and ready to execute by peer in any moment. 
 The output XML is fetch from the server side queue data (hsql)
 <pre>

 <u>Parameters:</u>Nil
 <u>Returns:</u>  
 <code>&lt;?xml version="1.0"?&gt;
 &lt;result&gt;
 	&lt;task status="EXCECUTING" id="1174"  trigger_time="6969068960689696" peer="dev-server"/&gt;
 	&lt;task status="EXCECUTING" id="1401"  trigger_time="6969068960689454" peer="4ecappcsg11"/&gt;
 &lt;/result&gt;

 </code>
 <u>Example:</u> http://server-ip-or-name/bldb/schedulerAPI?method=queue
 <hr> 
 </pre> 

 
 <h1>getPeerQueue</h1>
 This method lists all tasks that running on peers in the network.  
 The output XML generated from online peers response directly and no relation with the server queue data (hsql)
 <pre>

 <u>Parameters:</u>Nil
 <u>Returns:</u>  
 <code>&lt;?xml version="1.0"?&gt;
 &lt;result&gt;
 	&lt;task scheduler_id="21" trigger_time="1307426161000" trigger_time="1307426161656" /&gt;
 	&lt;task scheduler_id="22" trigger_time="1307426154000" trigger_time="1307426154656" /&gt;
 &lt;/result&gt;

 </code>
 <u>Example:</u> http://server-ip-or-name/bldb/schedulerAPI?method=getPeerQueue
 <hr> 
 </pre> 

 
     
 
 <h1>syncContract</h1>
 This method synchronize bloomberg data with Market Contracts those mapped with reference database. 
 Accepts optional parameter.<br>
 Parameter <b>contract</b> synchronizes only specified contract<br>

 Nil parameter synchronizes all new contracts that haven't synchronized so far 
 <pre>
 <u>Parameters:</u>[contract=<code>&lt;contract_ticker&gt;</code>] if no parameter specified, this synchronizes all new contracts those haven't synchronized yet.
 <u>Returns:</u>  
 <code>&lt;?xml version="1.0"?&gt;
 &lt;result&gt;
   &lt;status&gt;true/false&lt;/status&gt;
   &lt;contract&gt;CLZ01&lt;/contract&gt;
   &lt;contract&gt;CLZ02&lt;/contract&gt;
   ...
 &lt;/result&gt;

 </code>
 <u>Example:</u> 
 http://server-ip-or-name/bldb/schedulerAPI?method=syncContract&contract=CL010
 http://server-ip-or-name/bldb/schedulerAPI?method=syncContract 
 <hr> 
 </pre> 
 
 
 
 
 
 <h1>syncSecurity</h1>
 This method synchronize bloomberg data with Securities those mapped with reference database. 
 Accepts optional parameter.<br>
 Parameter <b>security</b> synchronizes only specified contract<br>

 Nil parameter synchronizes all new securities that haven't synchronized so far 
 <pre>
 <u>Parameters:</u>[security=<code>&lt;security_ticker&gt;</code>] if no parameter specified, this synchronizes all new securities those haven't synchronized yet.
 <u>Returns:</u>  
 <code>&&lt;?xml version="1.0"?&gt;
 &lt;result&gt;
   &lt;status&gt;true/false&lt;/status&gt;
   &lt;security&gt;CLZ01&lt;/security&gt;
   &lt;security&gt;CLZ02&lt;/security&gt;
   ...
 &lt;/result&gt;
 </code>
 <u>Example:</u> 
 http://server-ip-or-name/bldb/schedulerAPI?method=syncSecurity&contract=AIGC
 http://server-ip-or-name/bldb/schedulerAPI?method=syncSecurity 
 <hr>
 </pre>
 
 
 
 <h1>restartPeer</h1>

 Restarts peer application immediately, it restarts tomcat, REngine and JVM.  All the tasks that are currently running in the peer will be killed. 
 <pre>
 <u>Parameters:</u>peer=&lt;peername&gt;
 <u>Returns:</u>  
 <code>&lt;?xml version="1.0"?&gt;
 &lt;result&gt;&lt;status&gt;[Message]&lt;/status&gt;&lt;/result&gt;
 </code>

 <u>Example:</u> http://server-ip-or-name/bldb/schedulerAPI?method=restartPeer&peer=4ecappcsg10
 <hr> 
 </pre>
 
 
 
 
 
 
 
 <h1>restartAfterDone</h1>

 Restarts peer application once all tasks are completed, after this request made there won't be any further task will be sent to this peer.
 This request will restarts tomcat, REngine and JVM.  All the tasks that are currently running in the peer will be completed before restarting until if you specify the id to be killed if you think it is stuck on the peer 
 <pre>
 <u>Parameters:</u>peer=&lt;peername&gt;[kill=&lt;task_id&gt;]
 <u>Returns:</u>  
 <code>&lt;?xml version="1.0"?&gt;
 &lt;result&gt;&lt;status&gt;[Message]&lt;/status&gt;&lt;/result&gt;
 </code>

 <u>Example1:</u> http://server-ip-or-name/bldb/schedulerAPI?method=restartAfterDone&peer=4ecappcsg10
 <u>Example2:</u> http://server-ip-or-name/bldb/schedulerAPI?method=restartAfterDone&peer=4ecappcsg10&kill=1205
 <hr> 
 </pre>
 
<!--
<h1>restartServer</h1>

 This method restarts peer, it restarts tomcat and JVM of main server.
 <br><span style="color:red">Please careful when invoking this method, the tasks that are already triggered but waiting to be executed in the server queue will be lost and restarting process may take approximately 1-2 minute to rebuild the entire queue, any task scheduled during this time will be lost without trace</span>
 <pre>
 <u>Parameters:</u>Nil
 <u>Returns:</u>  
 <code>&lt;?xml version="1.0"?&gt;
 &lt;result&gt;&lt;status&gt;[Message]&lt;/status&gt;&lt;/result&gt;
 </code>

 <u>Example:</u> http://server-ip-or-name/bldb/schedulerAPI?method=restartServer
 <hr> 
 </pre>
 -->

<h1>addGroupIntoQueue</h1>

 Adding or activating group on directly on database, needs invoking this method, this method sychronizes the live queue with database changes.  
 <pre>
 <u>Parameters:</u>taskuid=&lt;unique indentifier of the task&gt;
 <u>Returns:</u>  
 <code>&lt;?xml version="1.0"?&gt;
 &lt;result&gt;&lt;status&gt;success or Error message in case of any error&lt;/status&gt;&lt;/result&gt;
 </code>

 <u>Example:</u> http://server-ip-or-name/bldb/schedulerAPI?method=addGroupIntoQueue&taskuid=rscriptlowpriority
 <hr> 
 </pre>

 

<h1>removeGroupFromQueue</h1>

 Removing or deactivating existing group on directly on database needs invoking this method, this method sychronizes the live queue with database changes.  
 <pre>
 <u>Parameters:</u>taskuid=&lt;unique indentifier of the task&gt;
 <u>Returns:</u>  
 <code>&lt;?xml version="1.0"?&gt;
 &lt;result&gt;&lt;status&gt;success or Error message in case of any error&lt;/status&gt;&lt;/result&gt;
 </code>

 <u>Example:</u> http://server-ip-or-name/bldb/schedulerAPI?method=removeGroupFromQueue&taskuid=rscriptlowpriority
 <hr> 
 </pre>


 
 <h1>getPeerPackages</h1>
 This method lists all R packages of peers that are currently online. 
 <pre>

 <u>Parameters:</u>Nil
 <u>Returns:</u>  
 <code>&lt;?xml version="1.0"?&gt;
 &lt;result&gt;
 	&lt;peer [packagename]=["version"] ..../&gt;
 	&lt;peer [packagename]=["version"] ..../&gt;
 &lt;/result&gt;

 </code>
 <u>Example:</u> http://server-ip-or-name/bldb/schedulerAPI?method=getPeerPackages
 <hr> 
 </pre> 

  <h1>getPeerInfo</h1>
 This method lists certain info of peers that are currently online. For example, peer software updated date, peer started time and etc... 
 <pre>

 <u>Parameters:</u>Nil
 <u>Returns:</u>  
 <code>&lt;?xml version="1.0"?&gt;
 &lt;result&gt;
 	&lt;peer [name]=["value"] ..../&gt;
 	&lt;peer [name]=["value"] ..../&gt;
 &lt;/result&gt;

 </code>
 <u>Example:</u> http://server-ip-or-name/bldb/schedulerAPI?method=getPeerInfo
 <hr> 
 </pre> 


  <h1>priorityQueue</h1>
 This method is to show or set the priority queue  
 <pre>

 <u>Parameters:</u>[enable=&lt;true/false&gt;] this parameter is optional
 <u>Returns:</u>  
 <code>&lt;?xml version="1.0"?&gt;
 &lt;result&gt;
 	&lt;status&gt;true/false&lt;/status&gt; 	
 &lt;/result&gt;

 </code>
 <u>Example: to show current settings</u> http://server-ip-or-name/bldb/schedulerAPI?method=priorityQueue
 <u>Example: to enable</u> http://server-ip-or-name/bldb/schedulerAPI?method=priorityQueue&enable=true
 <hr> 
 </pre>  
		
		
		
		
		
<h1>removeExecutingTask</h1>
 This method removes the task that is current showing as executing (rotating wheel), however this will remove only from the display and the server queue and it will not terminate if a task still executing on peer   
 <pre>
 	

 <u>Parameters:</u>task_id=&lt;number&gt;] Numerical id of the task
 <u>Parameters:</u>trigger_time=&lt;long integer&gt;] Long time in millisecond, this can be obtained from "queue" method.
 <u>Returns:</u>  
 <code>&lt;?xml version="1.0"?&gt;
 &lt;result&gt;
 	&lt;status&gt;status message&lt;/status&gt; 	
 &lt;/result&gt;

 </code>
 
 <u>Example:</u>http://server-ip-or-name/bldb/schedulerAPI?method=removeExecutingTask&task_id=9999&trigger_time=9080808878979943
 <hr> 
 </pre> 		
		

		
  <h1>getCurrentDateTime</h1>
 This method returns current date and time of the scheduler server
 <pre>

 <u>Parameters:</u>Nil
 <u>Returns:</u>  
 <code>&lt;?xml version="1.0"?&gt;
 &lt;result&gt;
 	&lt;time milliseconds="1308035953375" formatted="2011-06-14 15:19:13.375 SGT" ..../&gt;
 	
 &lt;/result&gt;

 </code>
 <u>Example:</u> http://server-ip-or-name/bldb/schedulerAPI?method=getCurrentDateTime
 <hr> 
 </pre> 
		


<h1>strategyXML</h1>
 This method gets Strategy XML from iExec  
  <pre>

 <u>Parameters:</u>strategy=&lt;strategy name&gt;  
 <u>Parameters:</u>contract=&lt;contract name&gt;
 <u>Returns:</u>  
 <code> Strategy in XML format 
 </code>
 
  <u>Example:</u>
   http://server-ip-or-name/bldb/schedulerAPI?method=strategyXML&stragegy=IDS&contract=CL1
 <hr> 
 
 </pre> 
			
</body>
</html>