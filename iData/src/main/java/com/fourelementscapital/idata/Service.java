package com.fourelementscapital.idata;

import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONObject;

import com.fourelementscapital.db.ReferenceDB;

public class Service extends ServiceAbstract {
	
	private class ApiResponse {
		
		public Response show(JSONObject message, Integer resCode) {
			return Response.status(resCode)
					.entity(message)
					.type(MediaType.APPLICATION_JSON)
					.build();
		}
	}
	
	private ApiResponse response = new ApiResponse();
	
	// LIST BB, CONTRACTS, SECURITIES, FUNDAMENTALS
	
	public Response getListTrading(String trading) throws Exception {
		JSONObject jsonObj = new JSONObject();
		List<Map<String,String>> result = null;
		String mtrading = trading.toLowerCase();
		
		try{
			jsonObj.put("result", "Internal Server Error");
			
			ReferenceDB ref = ReferenceDB.getReferenceDB();
			try {
				ref.connectDB();
				
				if (mtrading.equals("bb")) {
					result = ref.getListBB();
				} else if (mtrading.equals("securities")) {
					result = ref.getListSecurities();
				} else if (mtrading.equals("fundamentals")) {
					result = ref.getListFundamentals();
				} else if (mtrading.equals("contracts")) {
					result = ref.getListContracts();
				}
				
				 
			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				ref.closeDB();
			}
			
			jsonObj.put("result", result);
			
			
			return response.show(jsonObj, 200);
				
		}catch(Exception e){
			
			e.printStackTrace();
			return response.show(jsonObj, 503);
		}
	}
	
	// CONTRACTS
	
	public Response getDetailContracts(String symbol) throws Exception {
		JSONObject jsonObj = new JSONObject();
		Map<String,String> result = null;
		
		try{
			jsonObj.put("result", "Internal Server Error");
			
			ReferenceDB ref = ReferenceDB.getReferenceDB();
			try {
				ref.connectDB();
				result = ref.getDetailContracts(symbol);
			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				ref.closeDB();
			}

			jsonObj.put("result", result);
			
			return response.show(jsonObj, 200);
				
		}catch(Exception e){
			
			e.printStackTrace();
			return response.show(jsonObj, 503);
		}
	}
	
	public Response getAllPriceContract(String symbol) throws Exception {
		JSONObject jsonObj = new JSONObject();
		List<Map<String,Object>> result = null;
		
		try{
			jsonObj.put("result", "Internal Server Error");
			
			ReferenceDB ref = ReferenceDB.getReferenceDB();
			try {
				ref.connectDB();
				result = ref.getAllPriceContracts(symbol);
			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				ref.closeDB();
			}

			jsonObj.put("result", result);
			
			return response.show(jsonObj, 200);
				
		}catch(Exception e){
			
			e.printStackTrace();
			return response.show(jsonObj, 503);
		}
	}
	
	public Response getPriceContract(String pricetype, String symbol) throws Exception {
		JSONObject jsonObj = new JSONObject();
		List<Map<String,Object>> result = null;
		
		try{
			jsonObj.put("result", "Internal Server Error");
			
			ReferenceDB ref = ReferenceDB.getReferenceDB();
			try {
				ref.connectDB();
				if (pricetype.equals("low")) {
					result = ref.getLowPriceContracts(symbol);
				} else if (pricetype.equals("high")) {
					result = ref.getHighPriceContracts(symbol);
				} else if (pricetype.equals("open")) {
					result = ref.getOpenPriceContracts(symbol);
				} else if (pricetype.equals("close")) {
					result = ref.getClosePriceContracts(symbol);
				}
				
				 
			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				ref.closeDB();
			}

			jsonObj.put("result", result);
			
			return response.show(jsonObj, 200);
				
		}catch(Exception e){
			
			e.printStackTrace();
			return response.show(jsonObj, 503);
		}
	}
	
	// BB

	public Response getDetailBB(String symbol) throws Exception {
		JSONObject jsonObj = new JSONObject();
		Map<String,String> result = null;
		
		try{
			jsonObj.put("result", "Internal Server Error");
			
			ReferenceDB ref = ReferenceDB.getReferenceDB();
			try {
				ref.connectDB();
				result = ref.getDetailBB(symbol);
			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				ref.closeDB();
			}

			jsonObj.put("result", result);
			
			return response.show(jsonObj, 200);
				
		}catch(Exception e){
			
			e.printStackTrace();
			return response.show(jsonObj, 503);
		}
	}
	
	public Response getPriceBB(String symbol) throws Exception {
		JSONObject jsonObj = new JSONObject();
		List<Map<String,Object>> result = null;
		
		try{
			jsonObj.put("result", "Internal Server Error");
			
			ReferenceDB ref = ReferenceDB.getReferenceDB();
			try {
				ref.connectDB();
				result = ref.getPriceBB(symbol);
			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				ref.closeDB();
			}

			jsonObj.put("result", result);
			
			return response.show(jsonObj, 200);
				
		}catch(Exception e){
			
			e.printStackTrace();
			return response.show(jsonObj, 503);
		}
	}
	
	//FUNDAMENTALS

	public Response getDetailFundamentals(String symbol) throws Exception {
		JSONObject jsonObj = new JSONObject();
		Map<String,String> result = null;
		
		try{
			jsonObj.put("result", "Internal Server Error");
			
			ReferenceDB ref = ReferenceDB.getReferenceDB();
			try {
				ref.connectDB();
				result = ref.getDetailFundamentals(symbol);
			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				ref.closeDB();
			}

			jsonObj.put("result", result);
			
			return response.show(jsonObj, 200);
				
		}catch(Exception e){
			
			e.printStackTrace();
			return response.show(jsonObj, 503);
		}
	}
	
	public Response getAllPriceFundamental(String symbol) throws Exception {
		JSONObject jsonObj = new JSONObject();
		List<Map<String,Object>> result = null;
		
		try{
			jsonObj.put("result", "Internal Server Error");
			
			ReferenceDB ref = ReferenceDB.getReferenceDB();
			try {
				ref.connectDB();
				result = ref.getAllPriceFund(symbol);
			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				ref.closeDB();
			}

			jsonObj.put("result", result);
			
			return response.show(jsonObj, 200);
				
		}catch(Exception e){
			
			e.printStackTrace();
			return response.show(jsonObj, 503);
		}
	}
	
	public Response getPriceFundamental(String pricetype,String symbol) throws Exception {
		JSONObject jsonObj = new JSONObject();
		List<Map<String,Object>> result = null;
		
		try{
			jsonObj.put("result", "Internal Server Error");
			
			ReferenceDB ref = ReferenceDB.getReferenceDB();
			try {
				ref.connectDB();
				if (pricetype.equals("low")) {
					result = ref.getLowPriceFund(symbol);
				} else if (pricetype.equals("high")) {
					result = ref.getHighPriceFund(symbol);
				} else if (pricetype.equals("open")) {
					result = ref.getOpenPriceFund(symbol);
				} else if (pricetype.equals("close")) {
					result = ref.getClosePriceFund(symbol);
				}
				
				 
			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				ref.closeDB();
			}

			jsonObj.put("result", result);
			
			return response.show(jsonObj, 200);
				
		}catch(Exception e){
			
			e.printStackTrace();
			return response.show(jsonObj, 503);
		}
	}
	
	//SECURITIES

	public Response getDetailSecurities(String symbol) throws Exception {
		JSONObject jsonObj = new JSONObject();
		Map<String,String> result = null;
		
		try{
			jsonObj.put("result", "Internal Server Error");
			
			ReferenceDB ref = ReferenceDB.getReferenceDB();
			try {
				ref.connectDB();
				result = ref.getDetailSecurities(symbol);
			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				ref.closeDB();
			}

			jsonObj.put("result", result);
			
			return response.show(jsonObj, 200);
				
		}catch(Exception e){
			
			e.printStackTrace();
			return response.show(jsonObj, 503);
		}
	}
	
	public Response getAllPriceSecurities(String symbol) throws Exception {
		JSONObject jsonObj = new JSONObject();
		List<Map<String,Object>> result = null;
		
		try{
			jsonObj.put("result", "Internal Server Error");
			
			ReferenceDB ref = ReferenceDB.getReferenceDB();
			try {
				ref.connectDB();
				result = ref.getAllPriceSec(symbol);
			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				ref.closeDB();
			}

			jsonObj.put("result", result);
			
			return response.show(jsonObj, 200);
				
		}catch(Exception e){
			
			e.printStackTrace();
			return response.show(jsonObj, 503);
		}
	}
	
	public Response getPriceSecurities(String pricetype, String symbol) throws Exception {
		JSONObject jsonObj = new JSONObject();
		List<Map<String,Object>> result = null;
		
		try{
			jsonObj.put("result", "Internal Server Error");
			
			ReferenceDB ref = ReferenceDB.getReferenceDB();
			try {
				ref.connectDB();
				if (pricetype.equals("low")) {
					result = ref.getLowPriceSec(symbol);
				} else if (pricetype.equals("high")) {
					result = ref.getHighPriceSec(symbol);
				} else if (pricetype.equals("open")) {
					result = ref.getOpenPriceSec(symbol);
				} else if (pricetype.equals("close")) {
					result = ref.getClosePriceSec(symbol);
				}
				
				 
			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				ref.closeDB();
			}

			jsonObj.put("result", result);
			
			return response.show(jsonObj, 200);
				
		}catch(Exception e){
			
			e.printStackTrace();
			return response.show(jsonObj, 503);
		}
	}

	
}