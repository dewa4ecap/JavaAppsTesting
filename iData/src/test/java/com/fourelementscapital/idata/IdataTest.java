/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.idata;

import junit.framework.TestCase;


public class IdataTest extends TestCase {
	
    public void testGetListTrading() throws Exception
    {
    	Service service = new Service();
        assertEquals(200, service.getListTrading("bb").getStatus());
    }
    
    // Contracts
    
    public void testGetDetailContracts() throws Exception
    {
    	Service service = new Service();
        assertEquals(200, service.getDetailContracts("GCF18 Comdty").getStatus());
    }
    
    public void testGetPriceContracts() throws Exception
    {
    	Service service = new Service();
        assertEquals(200, service.getPriceContract("open","GCF18 Comdty").getStatus());
    }
    
    // BB
    
    public void testGetDetailBB() throws Exception
    {
    	Service service = new Service();
        assertEquals(200, service.getDetailBB("GC").getStatus());
    }
    
    public void testGetPriceBB() throws Exception
    {
    	Service service = new Service();
        assertEquals(200, service.getPriceBB("GC").getStatus());
    }
    
    // Fundamentals
    
    public void testGetDetailFundamentals() throws Exception
    {
    	Service service = new Service();
        assertEquals(200, service.getDetailFundamentals("BTCUSD").getStatus());
    }
    
    public void testGetPriceFundamentals() throws Exception
    {
    	Service service = new Service();
        assertEquals(200, service.getPriceFundamental("open","BTCUSD").getStatus());
    }
    
    // Securities
    
    public void testGetDetailSecurities() throws Exception
    {
    	Service service = new Service();
        assertEquals(200, service.getDetailSecurities("CEI1GCOL Index").getStatus());
    }
    
    public void testGetPriceSecurities() throws Exception
    {
    	Service service = new Service();
        assertEquals(200, service.getPriceSecurities("close","CEI1GCOL Index").getStatus());
    }
    
}