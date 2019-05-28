package com.fourelementscapital.imonitor.process;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

//Scheduler class that to ping external server 
public class AliveMessageScheduler {
	private final ScheduledExecutorService scheduler =  Executors.newScheduledThreadPool(1);
	private long INITIAl_DELAY_IN_MINUTES = 1;
	private long DELAY_IN_MINUTES = 1;

	public void sendIsAlive() {
		final Runnable isAlive = new Runnable() {
			public void run() {
				//TODO: code to ping to external server
			}
		};
		
		scheduler.scheduleWithFixedDelay(isAlive, INITIAl_DELAY_IN_MINUTES, DELAY_IN_MINUTES, TimeUnit.MINUTES);
	}
}
