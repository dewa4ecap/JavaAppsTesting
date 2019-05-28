package com.fourelementscapital.dataviewerapp;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.PathParam;
import javax.ws.rs.FormParam;
import com.fourelementscapital.restapi.DataViewerService;

@Path("/")
public class ApiController {
	
	// LIST MARKET
	
	@POST
	@Path("action/get/list/{market}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getListTrading(@PathParam("market") String market) throws Exception {
		
		DataViewerService service = new DataViewerService();
		return service.getListTrading(market, Params.getAlsidKey(),Params.getAluserKey());
	}
	
	//CONTRACTS
	
	@POST
	@Path("action/get/detail/contracts/{symbol}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getDetailContract(@PathParam("symbol") String symbol) throws Exception {
		
		DataViewerService service = new DataViewerService();
		return service.getDetailContracts(symbol, Params.getAlsidKey(),Params.getAluserKey());
	}
	
	@POST
	@Path("action/get/{pricetype}/contracts/{symbol}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getPriceContract(@PathParam("pricetype") String pricetype, @PathParam("symbol") String symbol) throws Exception {
		
		DataViewerService service = new DataViewerService();
		return service.getPriceContract(pricetype,symbol, Params.getAlsidKey(),Params.getAluserKey());
	}
	
	@POST
	@Path("action/get/allprice/contracts/{symbol}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllPriceContract(@PathParam("symbol") String symbol) throws Exception {
		
		DataViewerService service = new DataViewerService();
		return service.getAllPriceContract(symbol, Params.getAlsidKey(),Params.getAluserKey());
	}
	
	//BB
	
	@POST
	@Path("action/get/detail/bb/{symbol}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getDetailBB(@PathParam("symbol") String symbol) throws Exception {
		
		DataViewerService service = new DataViewerService();
		return service.getDetailBB(symbol, Params.getAlsidKey(),Params.getAluserKey());
	}
	
	@POST
	@Path("action/get/price/bb/{symbol}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getPriceBB(@PathParam("symbol") String symbol) throws Exception {
		
		DataViewerService service = new DataViewerService();
		return service.getPriceBB(symbol, Params.getAlsidKey(),Params.getAluserKey());
	}
	
	
	//FUNDAMENTAL
	
	@POST
	@Path("action/get/detail/fundamentals/{symbol}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getDetailFundamental(@PathParam("symbol") String symbol) throws Exception {
		
		DataViewerService service = new DataViewerService();
		return service.getDetailFundamentals(symbol, Params.getAlsidKey(),Params.getAluserKey());
	}
	
	@POST
	@Path("action/get/{pricetype}/fundamentals/{symbol}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getPriceFundamental(@PathParam("pricetype") String pricetype, @PathParam("symbol") String symbol) throws Exception {
		
		DataViewerService service = new DataViewerService();
		return service.getPriceFundamental(pricetype, symbol, Params.getAlsidKey(),Params.getAluserKey());
	}
	
	@POST
	@Path("action/get/allprice/fundamentals/{symbol}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllPriceFundamental(@PathParam("symbol") String symbol) throws Exception {
		
		DataViewerService service = new DataViewerService();
		return service.getAllPriceFundamental(symbol, Params.getAlsidKey(),Params.getAluserKey());
	}
	
	//SECURITIES
	
	@POST
	@Path("action/get/detail/securities/{symbol}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getDetailSecurities(@PathParam("symbol") String symbol) throws Exception {
		
		DataViewerService service = new DataViewerService();
		return service.getDetailSecurities(symbol, Params.getAlsidKey(),Params.getAluserKey());
	}

	@POST
	@Path("action/get/{pricetype}/securities/{symbol}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getLowPriceSecurities(@PathParam("pricetype") String pricetype, @PathParam("symbol") String symbol) throws Exception {
		
		DataViewerService service = new DataViewerService();
		return service.getPriceSecurities(pricetype, symbol, Params.getAlsidKey(),Params.getAluserKey());
	}
	
	@POST
	@Path("action/get/allprice/securities/{symbol}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllPriceSecurities(@PathParam("symbol") String symbol) throws Exception {
		
		DataViewerService service = new DataViewerService();
		return service.getAllPriceSecurities(symbol, Params.getAlsidKey(),Params.getAluserKey());
	}

	
}