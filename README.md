# uumind-log4j-appender-redis
Log4j appender for pushing log messages to a Redis list.

Derived from log4j-redis-appender(https://github.com/ryantenney/log4j-redis-appender)

## Configuration

This appender pushes log messages to a Redis list. Here is an example configuration:

	log4j.rootLogger=info, redis
	log4j.appender.redis=com.uumind.log4j.appender.redis.SimpleRedisAppender
	log4j.appender.redis.layout=org.apache.log4j.PatternLayout
	log4j.appender.redis.layout.ConversionPattern = [%p] %-d{yyyy-MM-dd HH:mm:ss} %l %m%n
	log4j.appender.redis.host=192.168.1.254
	log4j.appender.redis.port=6380
	log4j.appender.redis.key=logs

Where:

* **key** (_required_) key of the list to push log messages
* **host** (optional, default: localhost)
* **port** (optional, default: 6379)
* **password** (optional) redis password, if required

### Maven

```xml
<dependency>
	<groupId>com.uumind</groupId>
	<artifactId>uumind-log4j-appender-redis</artifactId>
	<version>0.0.1-SNAPSHOT</version>
</dependency>
```

### License

Copyright (c) 2012-2013 lendo

Published under Apache Software License 2.0, see LICENSE

