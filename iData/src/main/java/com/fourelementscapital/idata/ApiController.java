package com.fourelementscapital.idata;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/")
public class ApiController {
	
	@GET
	@Path("get/list/{trading}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getListTrading(@PathParam("trading") String trading) throws Exception {
		
		Service service = new Service();
		return service.getListTrading(trading).getEntity().toString();
	}
	
	//CONTRACTS
	
	@GET
	@Path("get/detail/contracts/{symbol}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getDetailContract(@PathParam("symbol") String symbol) throws Exception {
		
		Service service = new Service();
		return service.getDetailContracts(symbol).getEntity().toString();
	}
	
	@GET
	@Path("get/{pricetype}/contracts/{symbol}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getPriceContract(@PathParam("pricetype") String pricetype, @PathParam("symbol") String symbol) throws Exception {
		
		Service service = new Service();
		return service.getPriceContract(pricetype,symbol).getEntity().toString();
	}
	
	@GET
	@Path("get/allprice/contracts/{symbol}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getAllPriceContract(@PathParam("symbol") String symbol) throws Exception {
		
		Service service = new Service();
		return service.getAllPriceContract(symbol).getEntity().toString();
	}
	
	//BB
	
	@GET
	@Path("get/detail/bb/{symbol}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getDetailBB(@PathParam("symbol") String symbol) throws Exception {
		
		Service service = new Service();
		return service.getDetailBB(symbol).getEntity().toString();
	}
	
	@GET
	@Path("get/price/bb/{symbol}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getPriceBB(@PathParam("symbol") String symbol) throws Exception {
		
		Service service = new Service();
		return service.getPriceBB(symbol).getEntity().toString();
	}
	
	
	//FUNDAMENTAL
	
	@GET
	@Path("get/detail/fundamentals/{symbol}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getDetailFundamental(@PathParam("symbol") String symbol) throws Exception {
		
		Service service = new Service();
		return service.getDetailFundamentals(symbol).getEntity().toString();
	}
	
	@GET
	@Path("get/{pricetype}/fundamentals/{symbol}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getPriceFundamental(@PathParam("pricetype") String pricetype, @PathParam("symbol") String symbol) throws Exception {
		
		Service service = new Service();
		return service.getPriceFundamental(pricetype, symbol).getEntity().toString();
	}
	
	@GET
	@Path("get/allprice/fundamentals/{symbol}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getAllPriceFundamental(@PathParam("symbol") String symbol) throws Exception {
		
		Service service = new Service();
		return service.getAllPriceFundamental(symbol).getEntity().toString();
	}
	
	//SECURITIES
	
	@GET
	@Path("get/detail/securities/{symbol}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getDetailSecurities(@PathParam("symbol") String symbol) throws Exception {
		
		Service service = new Service();
		return service.getDetailSecurities(symbol).getEntity().toString();
	}

	@GET
	@Path("get/{pricetype}/securities/{symbol}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getLowPriceSecurities(@PathParam("pricetype") String pricetype, @PathParam("symbol") String symbol) throws Exception {
		
		Service service = new Service();
		return service.getPriceSecurities(pricetype, symbol).getEntity().toString();
	}
	
	@GET
	@Path("get/allprice/securities/{symbol}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getAllPriceSecurities(@PathParam("symbol") String symbol) throws Exception {
		
		Service service = new Service();
		return service.getAllPriceSecurities(symbol).getEntity().toString();
	}
	
}