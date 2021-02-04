package com.zsj.config;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.zsj.service.MonitoredCallerRunsPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

@Configuration
public class CustomThreadPoolConfig {

    @Bean(name = "imMsgExecutor")
    public Executor imMsgExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(25);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(1000);
        executor.setKeepAliveSeconds(2 * 60);
        executor.setThreadNamePrefix("imMsgExecutor-pool-");
        //线程池关闭等待所有任务都完成再继续销毁其他的Bean
        executor.setWaitForTasksToCompleteOnShutdown(true);
        //线程池中任务的等待时间，如果超过这个时候还没有销毁就强制销毁
        executor.setAwaitTerminationSeconds(60);
        executor.setRejectedExecutionHandler(new MonitoredCallerRunsPolicy());
        return executor;
    }

    @Bean(name = "scheduledExecutor")
    public ScheduledExecutorService getScheduledExecutorService(){
        return new ScheduledThreadPoolExecutor(2,  new ThreadFactoryBuilder().setNameFormat("im-msg-scheduled").build());
    }

}
