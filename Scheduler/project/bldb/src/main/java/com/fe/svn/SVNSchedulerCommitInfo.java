/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fe.svn;

public class SVNSchedulerCommitInfo {

	private long revision;
	private int scheduler_id;
	
	public long getRevision() {
		return revision;
	}
	public void setRevision(long revision) {
		this.revision = revision;
	}
	public int getScheduler_id() {
		return scheduler_id;
	}
	public void setScheduler_id(int scheduler_id) {
		this.scheduler_id = scheduler_id;
	}
	
}


