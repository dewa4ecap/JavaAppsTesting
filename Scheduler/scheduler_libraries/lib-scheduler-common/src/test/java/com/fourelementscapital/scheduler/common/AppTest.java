/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.scheduler.common;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for App
 */
public class AppTest extends TestCase {

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    /**
     * Empty Test
     */
    public void testApp()
    {
        assertTrue( true );
    }

}


