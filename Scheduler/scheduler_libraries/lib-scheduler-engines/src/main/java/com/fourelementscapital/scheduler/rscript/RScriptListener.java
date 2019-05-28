/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.scheduler.rscript;

import com.fourelementscapital.scheduler.rscript.RScript;

public interface RScriptListener {

	public void onScriptSent(RScript rscript, String peer) throws Exception ;
	public void onScriptFinished(RScript rscript, String peer, String result, String status) throws Exception;
	public void onScriptTimedOut(RScript rscript) throws Exception;
	
}


