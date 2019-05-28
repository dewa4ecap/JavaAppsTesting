package com.fourelementscapital.imonitor.queue;

import java.lang.Thread.State;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fourelementscapital.imonitor.entities.Alarm;
import com.fourelementscapital.imonitor.utils.EmailUtils;
import com.fourelementscapital.imonitor.utils.PhoneCallUtils;
import com.fourelementscapital.imonitor.utils.TextToSpeachUtils;

import java.util.concurrent.Future;


/*Notes from Gama :
Lambda expression (-> sign) only exist on Java 8 and above.*/

public class IMonitorQueue {

	// https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ConcurrentHashMap.html
	private static final Logger log = LogManager.getLogger(IMonitorQueue.class.getName());
	private static ConcurrentHashMap<String, Alarm> iMonitorQueue = new ConcurrentHashMap<String, Alarm>();
	private static Thread iMonitorQueueThread = null;
	public static boolean pauseiMonitorQueueThread = false;
	final public static Object iMonitorQueueLock = new Object();
	static int numTimeBetweenBatches = 100;
	 protected static ExecutorService threadPool = Executors.newFixedThreadPool(10);
	public static void AddDataToQueue(Alarm alarm) {
		synchronized(iMonitorQueueLock) {
			//Don't add it if key is already in the queue.  Reads are much faster that writes since it doesn't need to lock the ConcurrentHashMap
			String key = alarm.getSubject() + alarm.getIpAddress();
			if (iMonitorQueue.get(key) == null) { 
				iMonitorQueue.put(key, alarm);
			}
		    if ((!pauseiMonitorQueueThread) && (iMonitorQueueThread == null || iMonitorQueueThread.getState() == State.TERMINATED)) {
		    	iMonitorQueueThread = buildiMonitorQueueThread();
		    	iMonitorQueueThread.start();
		    }
		}
	}
	
	private static Thread buildiMonitorQueueThread() {
		Thread retVal = null;
		if (!pauseiMonitorQueueThread) {
			retVal =  new Thread("processiMonitorQueueThread") {

				public void run() {
					try {
						boolean continueLoop = true;
						while (continueLoop) {
									ConcurrentHashMap<String, Alarm> toProcess;
									synchronized(iMonitorQueueLock) {
										toProcess = iMonitorQueue;
										iMonitorQueue = new ConcurrentHashMap<String, Alarm>();
									}
									for(final Map.Entry<String, Alarm> newAlarm : toProcess.entrySet()) {
										
										  //Java 8
										  /*threadPool.submit(() -> {
											  try {
												    Alarm alarm = newAlarm.getValue();
													if (BooleanUtils.isTrue(alarm.getPhoneCall())) PhoneCallUtils.makePhoneCall(alarm);
													if (BooleanUtils.isTrue(alarm.getSayIt())) TextToSpeachUtils.speak(alarm.getSubject());
													if (BooleanUtils.isTrue(alarm.getEmailIt())) EmailUtils.sendMail(alarm);
												} catch (Exception e) {
													log.log(Level.ERROR, e);
												}
										  });*/
										
										// Modified to Java 7
										Future future = threadPool.submit(new Runnable() {
											public void run() {
												try {
												    Alarm alarm = newAlarm.getValue();
													if (BooleanUtils.isTrue(alarm.getPhoneCall())) PhoneCallUtils.makePhoneCall(alarm);
													if (BooleanUtils.isTrue(alarm.getSayIt())) TextToSpeachUtils.speak(alarm.getSubject());
													if (BooleanUtils.isTrue(alarm.getEmailIt())) EmailUtils.sendMail(alarm);
												} catch (Exception e) {
													log.log(Level.ERROR, e);
												}
											}
										});
										
									}

							Thread.sleep(numTimeBetweenBatches);
							synchronized(iMonitorQueueLock) {
								continueLoop = (iMonitorQueue.size() > 0);
							}
						}
					} catch (Exception e) {
						log.log(Level.ERROR, e);
					}
				}
			};
		}
		return retVal;
		
	}
}
