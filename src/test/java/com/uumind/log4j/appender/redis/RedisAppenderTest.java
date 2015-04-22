package com.uumind.log4j.appender.redis;

import org.apache.log4j.Logger;

public class RedisAppenderTest {
	private final static Logger log = Logger.getLogger(RedisAppenderTest.class);
	
	public static void main(String[] args) throws Exception {
		for(int i=0;i<10000;i++) {
			log.info("Log Test");
		}
		
		System.out.println("Done");
		Thread.sleep(10000);
	}

}
