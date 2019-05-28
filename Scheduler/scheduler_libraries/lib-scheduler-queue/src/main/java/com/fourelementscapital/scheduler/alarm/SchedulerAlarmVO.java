/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.scheduler.alarm;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Value object used as scheduler data container
 */
public class SchedulerAlarmVO implements Serializable {
	
	private static final long serialVersionUID = 1L;

	/**
	 * Scheduler id
	 */
	private int schedulerId;

	/**
	 * Trigger time
	 */
	private long triggerTime;

	/**
	 * Alarm type : 'phone' or 'email'
	 */
	private String alarmType;
	
	/**
	 * Task's name
	 */
	private String name;
	
	/**
	 * Alarm subject : 'Failed', 'Timeout', or 'Crashed'
	 */	
	private String subject;
	
	/**
	 * Message field contains Error log
	 */	
	private String message;
	
	/**
	 * From field contains Peer computer name
	 */	
	private String from;
	
	/**
	 * Error code
	 */
	private int errCode;
	
	/**
	 * Is the error instance of ExceptionSchedulerTeamRelated
	 */	
	private boolean isExceptionSchedulerTeamRelated;
	
	/**
	 * Peer computer name
	 */	
	private String computerName;
	
	/**
	 * Console message
	 */	
	private String consoleMsg;
	
	/**
	 * Execution logs
	 */	
	private List<Map> execLogs;
	
	/**
	 * Is RepCode exist
	 */	
	private boolean isRepCodeExist;
	
	/**
	 * Task's theme tags
	 */	
	private List themeTags;
	
	/**
	 * Task's owner theme
	 */	
	private String ownerTheme;
	
	/**
	 * Task's queue log
	 */	
	private Map queueLog;
	
	/**
	 * Peer friendly name
	 */	
	private String peerFriendlyName;
	
	/**
	 * Get scheduler id
	 * @return scheduler id
	 */
	public int getSchedulerId() {
		return schedulerId;
	}

	/**
	 * Set scheduler id
	 * @param schedulerId scheduler id
	 */	
	public void setSchedulerId(int schedulerId) {
		this.schedulerId = schedulerId;
	}

	/**
	 * Get trigger time
	 * @return trigger time
	 */
	public long getTriggerTime() {
		return triggerTime;
	}

	/**
	 * Set trigger time
	 * @param triggerTime trigger time
	 */	
	public void setTriggerTime(long triggerTime) {
		this.triggerTime = triggerTime;
	}

	/**
	 * Get alarm type
	 * @return alarm type
	 */
	public String getAlarmType() {
		return alarmType;
	}
	
	/**
	 * Set alarm type
	 * @param alarmType alarm type
	 */
	public void setAlarmType(String alarmType) {
		this.alarmType = alarmType;
	}
	
	/**
	 * Get name
	 * @return name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Set name
	 * @param name name
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Get subject
	 * @return subject
	 */
	public String getSubject() {
		return subject;
	}
	
	/**
	 * Set subject
	 * @param subject subject
	 */
	public void setSubject(String subject) {
		this.subject = subject;
	}
	
	/**
	 * Get message
	 * @return message
	 */
	public String getMessage() {
		return message;
	}
	
	/**
	 * Set message
	 * @param message message
	 */
	public void setMessage(String message) {
		this.message = message;
	}
	
	/**
	 * Get from
	 * @return from
	 */
	public String getFrom() {
		return from;
	}
	
	/**
	 * Set from
	 * @param from from
	 */
	public void setFrom(String from) {
		this.from = from;
	}
	
	/**
	 * Get error code
	 * @return error code
	 */
	public int getErrCode() {
		return errCode;
	}
	
	/**
	 * Set error code
	 * @param errCode errCode
	 */
	public void setErrCode(int errCode) {
		this.errCode = errCode;
	}
	
	/**
	 * Get is ExceptionSchedulerTeamRelated class
	 * @return true if ExceptionSchedulerTeamRelated class
	 */
	public boolean isExceptionSchedulerTeamRelated() {
		return isExceptionSchedulerTeamRelated;
	}
	
	/**
	 * Set is ExceptionSchedulerTeamRelated class
	 * @param isExceptionSchedulerTeamRelated is ExceptionSchedulerTeamRelated class
	 */
	public void setExceptionSchedulerTeamRelated(
			boolean isExceptionSchedulerTeamRelated) {
		this.isExceptionSchedulerTeamRelated = isExceptionSchedulerTeamRelated;
	}
	
	/**
	 * Get computer name
	 * @return computer name
	 */
	public String getComputerName() {
		return computerName;
	}
	
	/**
	 * Set computer name
	 * @param computerName computerName
	 */
	public void setComputerName(String computerName) {
		this.computerName = computerName;
	}
	
	/**
	 * Get console message
	 * @return console message
	 */
	public String getConsoleMsg() {
		return consoleMsg;
	}
	
	/**
	 * Set console message
	 * @param consoleMsg console message
	 */
	public void setConsoleMsg(String consoleMsg) {
		this.consoleMsg = consoleMsg;
	}
	
	/**
	 * Get execution logs
	 * @return execution logs
	 */
	public List<Map> getExecLogs() {
		return execLogs;
	}
	
	/**
	 * Set execution logs
	 * @param execLogs execution logs
	 */
	public void setExecLogs(List<Map> execLogs) {
		this.execLogs = execLogs;
	}
	
	/**
	 * Set is rep code exist
	 * @return true if rep code exist
	 */
	public boolean isRepCodeExist() {
		return isRepCodeExist;
	}
	
	/**
	 * Set is rep code exist
	 * @param isRepCodeExist is rep code exist
	 */
	public void setRepCodeExist(boolean isRepCodeExist) {
		this.isRepCodeExist = isRepCodeExist;
	}
	
	/**
	 * Get theme tags
	 * @return theme tags
	 */
	public List getThemeTags() {
		return themeTags;
	}
	
	/**
	 * Set theme tags
	 * @param themeTags theme tags
	 */
	public void setThemeTags(List themeTags) {
		this.themeTags = themeTags;
	}
	
	/**
	 * Get owner theme
	 * @return owner theme
	 */
	public String getOwnerTheme() {
		return ownerTheme;
	}
	
	/**
	 * Set owner theme
	 * @param ownerTheme owner theme
	 */
	public void setOwnerTheme(String ownerTheme) {
		this.ownerTheme = ownerTheme;
	}
	
	/**
	 * Get queue log
	 * @return queue log
	 */
	public Map getQueueLog() {
		return queueLog;
	}
	
	/**
	 * Set queue log
	 * @param queueLog queue log
	 */
	public void setQueueLog(Map queueLog) {
		this.queueLog = queueLog;
	}
	
	/**
	 * Get peer friendly name
	 * @return peer friendly name
	 */
	public String getPeerFriendlyName() {
		return peerFriendlyName;
	}
	
	/**
	 * Set peer friendly name
	 * @param peerFriendlyName peer friendly name
	 */
	public void setPeerFriendlyName(String peerFriendlyName) {
		this.peerFriendlyName = peerFriendlyName;
	}
	
}


