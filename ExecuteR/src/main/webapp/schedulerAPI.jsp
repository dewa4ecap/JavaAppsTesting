<html>
	<head>
		<title>Scheduler API</title>
	</head>
	<body>
		
	<div align="center" style="border:1px solid grey; padding:3px; font-size:3em; background-color:grey">SchedulerAPI</div>	
		<P>
 <h1>execSyncScript</h1>
 This method executes R Script synchronously in parallel computing and responds with xml as result.  
 <br><strong>This method accepts only POST method conneciton</strong>
 <pre>

 <u>Parameters:</u>script=&lt;R Script &gt;
 <u>Parameters:</u>uniquename=&lt;unique name to indentify in log&gt;
 <u>Parameters:</u>[executeAt=&lt;peername&gt;] exeample:4ecappcsg13  (this is optional parameter)   
 <u>Parameters:</u>[engine=&lt;rserve or rengine&gt;] default:rengine (this is optional parameter)
 <u>Returns:</u>  
 <code> xml result
 </code>
 <u>Example:</u> 
 It is recommended to use HTML form submission or alternatively use low leve HTTP POST Connection 
 <br><br>
 <a href="script.html">Click here </a> to open Script Testing Tool
 <hr> 
 </pre> 

<h1>execAsyncScript</h1>
 This method executes R Script asynchronously in parallel computing and responds with result token  
 <br><strong>This method accepts only POST method conneciton</strong>
 <pre>

 <u>Parameters:</u>script=&lt;R Script &gt;
 <u>Parameters:</u>uniquename=&lt;unique name to indentify in log&gt;
 <u>Parameters:</u>[executeAt=&lt;peername&gt;] exeample:4ecappcsg13  (this is optional parameter)   
 <u>Parameters:</u>[engine=&lt;rserve or rengine&gt;] default:rengine (this is optional parameter)
 <u>Returns:</u>  
 <code> result token plain text
 </code>
 Once you receive plain text token, subsequently request 
 the URL (get method)  http://server-ip-or-name/ExecuteR/schedulerAPI?method=getScriptResult&token=[result-token]
 this will return xml result of the executed script, if not ready it will return status  
 
 <u>Example:</u> 
 It is recommended to use HTML form submission or alternatively use low leve HTTP POST Connection 
 <br><br>
 <a href="script.html">Click here </a> to open Script Testing Tool
 <hr> 
 </pre> 

</body>
</html>