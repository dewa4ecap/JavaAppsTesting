/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.scheduler.queue;

import java.util.List;

import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.attribute.MultiValueNullableAttribute;
import com.googlecode.cqengine.attribute.SimpleAttribute;
import com.googlecode.cqengine.attribute.SimpleNullableAttribute;
import com.googlecode.cqengine.codegen.AttributesGenerator;

public class QueueStack {
	
	private String name=null;
	private List<String>  supportedtaskuids=null;
	private String peername=null;
	private int executioncount=0;
	private int priority=0;
	private long lastexecuted=0;
	private String uid="";
	private boolean running=false;
	private boolean available=false;
	
	public boolean isAvailable() {
		return available;
	}

	public void setAvailable(boolean available) {
		this.available = available;
	}

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

	public QueueStack(){
		
	}
	 
	public String getName() {
		return name;
	}

	public List<String> getSupportedtaskuids() {
		return supportedtaskuids;
	}

	public String getPeername() {
		return peername;
	}

	public int getExecutioncount() {
		return executioncount;
	}

	public int getPriority() {
		return priority;
	}

	public long getLastexecuted() {
		return lastexecuted;
	}

	public String getUid() {
		return uid;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setSupportedtaskuids(List<String> supportedtaskuids) {
		this.supportedtaskuids = supportedtaskuids;
	}

	public void setPeername(String peername) {
		this.peername = peername;
	}

	public void setExecutioncount(int executioncount) {
		this.executioncount = executioncount;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public void setLastexecuted(long lastexecuted) {
		this.lastexecuted = lastexecuted;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public static void main(String[] args) {
		System.out.println(AttributesGenerator.generateAttributesForPastingIntoTargetClass(QueueStack.class));
	}




    /**
     * CQEngine attribute for accessing field {@code QueueStack.name}.
     */
    // Note: For best performance:
    // - if this field cannot be null, replace this SimpleNullableAttribute with a SimpleAttribute
    public static final Attribute<QueueStack, String> NAME = new SimpleNullableAttribute<QueueStack, String>("NAME") {
        public String getValue(QueueStack queuestack) { return queuestack.name; }
    };

    /**
     * CQEngine attribute for accessing field {@code QueueStack.supportedtaskuids}.
     */
    // Note: For best performance:
    // - if the list cannot contain null elements change true to false in the following constructor, or
    // - if the list cannot contain null elements AND the field itself cannot be null, replace this
    //   MultiValueNullableAttribute with a MultiValueAttribute (and change getNullableValues() to getValues())
    public static final Attribute<QueueStack, String> SUPPORTEDTASKUIDS = new MultiValueNullableAttribute<QueueStack, String>("SUPPORTEDTASKUIDS", true) {
        public List<String> getNullableValues(QueueStack queuestack) { return queuestack.supportedtaskuids; }
    };

    /**
     * CQEngine attribute for accessing field {@code QueueStack.peername}.
     */
    // Note: For best performance:
    // - if this field cannot be null, replace this SimpleNullableAttribute with a SimpleAttribute
    public static final Attribute<QueueStack, String> PEERNAME = new SimpleNullableAttribute<QueueStack, String>("PEERNAME") {
        public String getValue(QueueStack queuestack) { return queuestack.peername; }
    };

    /**
     * CQEngine attribute for accessing field {@code QueueStack.executioncount}.
     */
    public static final Attribute<QueueStack, Integer> EXECUTIONCOUNT = new SimpleAttribute<QueueStack, Integer>("EXECUTIONCOUNT") {
        public Integer getValue(QueueStack queuestack) { return queuestack.executioncount; }
    };

    /**
     * CQEngine attribute for accessing field {@code QueueStack.priority}.
     */
    public static final Attribute<QueueStack, Integer> PRIORITY = new SimpleAttribute<QueueStack, Integer>("PRIORITY") {
        public Integer getValue(QueueStack queuestack) { return queuestack.priority; }
    };

    /**
     * CQEngine attribute for accessing field {@code QueueStack.lastexecuted}.
     */
    public static final Attribute<QueueStack, Long> LASTEXECUTED = new SimpleAttribute<QueueStack, Long>("LASTEXECUTED") {
        public Long getValue(QueueStack queuestack) { return queuestack.lastexecuted; }
    };

    /**
     * CQEngine attribute for accessing field {@code QueueStack.uid}.
     */
    // Note: For best performance:
    // - if this field cannot be null, replace this SimpleNullableAttribute with a SimpleAttribute
    public static final Attribute<QueueStack, String> UID = new SimpleNullableAttribute<QueueStack, String>("UID") {
        public String getValue(QueueStack queuestack) { return queuestack.uid; }
    };

    /**
     * CQEngine attribute for accessing field {@code QueueStack.running}.
     */
    public static final Attribute<QueueStack, Boolean> RUNNING = new SimpleAttribute<QueueStack, Boolean>("RUNNING") {
        public Boolean getValue(QueueStack queuestack) { return queuestack.running; }
    };

    /**
     * CQEngine attribute for accessing field {@code QueueStack.available}.
     */
    public static final Attribute<QueueStack, Boolean> AVAILABLE = new SimpleAttribute<QueueStack, Boolean>("AVAILABLE") {
        public Boolean getValue(QueueStack queuestack) { return queuestack.available; }
    };

 


}


