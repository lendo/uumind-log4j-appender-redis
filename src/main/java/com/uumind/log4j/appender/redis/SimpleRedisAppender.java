package com.uumind.log4j.appender.redis;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.spi.ErrorCode;
import org.apache.log4j.spi.LoggingEvent;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;

/**
 * 参考了：log4j-redis-appender(https://github.com/ryantenney/log4j-redis-appender)
 * 但是没有采用批量和定时任务的策略，只要有日志append进来，就立即写入redis.
 * 
 * 目前使用ConcurrentLinkedQueue来作为日志缓存队列
 * 且SimpleRedisAppender本身作为守护进程存在，所以如果日志高速入队列，可能造成队列数据尚未读取完成，则守护线程被中止的情况。
 * 下一步可以考虑使用持久化队列替代ConcurrentLinkedQueue，参考如下：
 * https://github.com/flipkart-incubator/Iris-BufferQueue
 * https://github.com/magro/persistent-queue
 * 
 * @author lendo
 *
 */
public class SimpleRedisAppender extends AppenderSkeleton implements Runnable {
	// (可选, 默认: localhost)
	private String host = "localhost";
	// (可选, 默认: 6379)
	private int port = 6379;
	// (可选) redis password, if required
	private String password;
	// 日志推送到redis的key
	private String key;
	
	private boolean daemonThread = true;

	private Queue<LoggingEvent> events;

	private JedisPool pool;

	private ExecutorService executor;

	@Override
	public void activateOptions() {
		try {
			super.activateOptions();

			if (key == null)
				throw new IllegalStateException("Must set 'key'");

			if (executor == null)
				executor = Executors.newFixedThreadPool(5, new NamedThreadFactory("RedisAppender", daemonThread));

			events = new ConcurrentLinkedQueue<LoggingEvent>();

			JedisPoolConfig config = new JedisPoolConfig();
		    config.setMaxTotal(10);
		    
		    if(password!=null && !password.isEmpty()) {
		    	pool = new JedisPool(config, host, port, Protocol.DEFAULT_TIMEOUT, password);
		    } else {
		    	pool = new JedisPool(config, host, port);
		    }
			
		} catch (Exception e) {
			LogLog.error("Error during activateOptions", e);
		}
	}

	@Override
	protected void append(LoggingEvent event) {
		try {
			populateEvent(event);
			events.add(event);
			executor.execute(this);
		} catch (Exception e) {
			errorHandler.error("Error populating event and adding to queue", e, ErrorCode.GENERIC_FAILURE, event);
		}
	}

	protected void populateEvent(LoggingEvent event) {
		event.getThreadName();
		event.getRenderedMessage();
		event.getNDC();
		event.getMDCCopy();
		event.getThrowableStrRep();
		event.getLocationInformation();
	}

	@Override
	public void close() {
		try {
			executor.shutdown();
			pool.destroy();
		} catch (Exception e) {
			errorHandler.error(e.getMessage(), e, ErrorCode.CLOSE_FAILURE);
		}
	}

	public void run() {
		try {
			LoggingEvent event;
			
			// 在判断events.poll()时，可能已经又有线程的log事件进入，则可能造成数据混乱，所以需要在pop出event时对events加锁
			// 参考：https://www.evernote.com/shard/s203/sh/883e0781-8bbf-4314-b4e4-d5bf1f7e16a3/c0c660d5e69b95b6d2a2a7c4d59435e6
			synchronized(events) {
				while ((event = events.poll()) != null) {
					try {
						Jedis jedis = pool.getResource();
						String message = layout.format(event);
						jedis.lpush(key, message);
						/*
						 * pool.returnResource() is deprecated in jedis 2.7.1
						 * starting from Jedis 3.0 pool.returnResource() won't exist. Resouce cleanup should be done using jedis.close()
						 */
						jedis.close();
					} catch (Exception e) {
						errorHandler.error(e.getMessage(), e, ErrorCode.GENERIC_FAILURE, event);
					}
				}
			}
		} catch (Exception e) {
			errorHandler.error(e.getMessage(), e, ErrorCode.WRITE_FAILURE);
		}
	}

	public void setHost(String host) {
		this.host = host;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public boolean requiresLayout() {
		return true;
	}

}