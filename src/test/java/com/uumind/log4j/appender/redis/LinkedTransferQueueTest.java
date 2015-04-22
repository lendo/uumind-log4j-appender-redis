package com.uumind.log4j.appender.redis;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedTransferQueue;

public class LinkedTransferQueueTest {

	public static void main(String[] args) throws Exception {
		final LinkedTransferQueue<String> q = new LinkedTransferQueue<String>();

		ExecutorService executor = Executors.newFixedThreadPool(10);
		
		for(int i=0;i<10;i++) {
			executor.execute(new Runnable(){

				@Override
				public void run() {
					try {
						String qs;
						while((qs=q.take())!=null) {
							System.out.println(qs);
						}
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
			});
		}
		
		for(int i=0;i<100;i++) {
			q.add("Log Test-" + i);
		}
		
		for(int i=0;i<100;i++) {
			q.add("222-" + i);
		}
	}

}
