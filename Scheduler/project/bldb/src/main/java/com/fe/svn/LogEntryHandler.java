/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fe.svn;

import java.util.Vector;

import org.tmatesoft.svn.core.ISVNDirEntryHandler;
import org.tmatesoft.svn.core.ISVNLogEntryHandler;
import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;

public class LogEntryHandler implements ISVNLogEntryHandler, ISVNDirEntryHandler{

	
	private Vector<SVNLogEntry> messages=null;
	private Vector<SVNDirEntry> dirmessages=null;
	
	private boolean arrivalStarted=false;
	public LogEntryHandler(){
		this.messages=new Vector<SVNLogEntry>();
		this.dirmessages=new Vector<SVNDirEntry>();
		
	}
	
	public void handleLogEntry(SVNLogEntry arg0) throws SVNException {
		
		this.messages.add(arg0);
		arrivalStarted=true;
	}
	
	
	public void handleDirEntry(SVNDirEntry arg0) throws SVNException {
		
		this.dirmessages.add(arg0);
		arrivalStarted=true;
	}
	
	
	public Vector<SVNLogEntry> getLogMessages() {
		return (!arrivalStarted)?null:messages;
	}

	public Vector<SVNDirEntry> getLogDirMessages() {
		return (!arrivalStarted)?null:dirmessages;
	}
}


