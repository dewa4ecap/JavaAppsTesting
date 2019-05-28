package com.fourelementscapital.idata;

import java.util.Map;
import java.util.Date;
import javax.ws.rs.core.Response;


public abstract class ServiceAbstract {
	
	//List BB, Contract, Fundamental, Securities
	public abstract Response getListTrading(String trading) throws Exception;
	
	public abstract Response getDetailBB(String symbol) throws Exception;
	public abstract Response getPriceBB(String symbol) throws Exception;
	
	public abstract Response getDetailContracts(String symbol) throws Exception;
	public abstract Response getPriceContract(String pricetype, String symbol) throws Exception;
	public abstract Response getAllPriceContract(String symbol) throws Exception;
	
	public abstract Response getDetailSecurities(String symbol) throws Exception;
	public abstract Response getPriceSecurities(String pricetype, String symbol) throws Exception;
	public abstract Response getAllPriceSecurities(String symbol) throws Exception;
	
	public abstract Response getDetailFundamentals(String symbol) throws Exception;
	public abstract Response getPriceFundamental(String pricetype, String symbol) throws Exception;
	public abstract Response getAllPriceFundamental(String symbol) throws Exception;
	
}