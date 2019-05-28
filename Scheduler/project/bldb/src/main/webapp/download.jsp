<%@ page contentType="text/html; charset=utf-8" language="java" import="com.fe.xls.*,org.jfree.chart.*,java.io.*" errorPage="" %>
<%
if(request.getParameter("ftables")!=null && request.getParameter("fnames") !=null){
	try{
		
	    //System.out.println("download.jsp called1::"+request.getParameter("ftables"));
	   // System.out.println("download.jsp dbkey::"+request.getParameter("dbkey"));
		
		XLStaticWriter xls=new XLStaticWriter(request.getParameter("dbkey"),request.getParameter("ftables"),request.getParameter("fnames"),request.getParameter("dquery"),request.getParameter("cquery"),request.getParameter("allcommodities"),request.getParameter("commodities"),request.getParameter("commString"));
		if(request.getParameter("chart")!=null && request.getParameter("chart").equalsIgnoreCase("yes")){
			xls.downloadGraph(response,request);
		}else{
			xls.download(response,request);
		}
		
		
	 		
	}catch(Exception e){
		e.printStackTrace();
	}
}else if(request.getParameter("xlid")!=null){
	try{
		XLStaticWriter.downloadLive(request,response);
	}catch(Exception e){
		e.printStackTrace();
	}

}else if(request.getParameter("chartid")!=null){
	try{
		
		JFreeChart chart=( JFreeChart)session.getAttribute(request.getParameter("chartid"));
		if(chart!=null){
	        response.setContentType("image/jpeg");
	        if(request.getParameter("jpgattachment")!=null){
	        	response.setHeader("Content-Disposition", "attachment; filename=\"" + "chart.jpg"+ "\"");
	        }
			OutputStream out1 = response.getOutputStream();			  
			ChartUtilities.writeChartAsJPEG(out1, chart, 1300, 680);			 
			out1.flush();
		}


	}catch(Exception e){
		e.printStackTrace();
	}


}else if(request.getParameter("templatekey")!=null){

	try{
		XLStaticWriter.downloadTemplate(request,response);
	}catch(Exception e){
		e.printStackTrace();
	}


}else if(request.getParameter("tradingxls")!=null){


	XLSTradeDataWriter xls1=new XLSTradeDataWriter(request.getParameter("dquery1"),request.getParameter("cquery1"),request.getParameter("commodities1"),request.getParameter("commString1"),request.getParameter("allcommodities1"),request.getParameter("data_output_raw"),request.getParameter("tradesource"),request.getParameter("accounts"));
	xls1.download(response,request);
}
%>