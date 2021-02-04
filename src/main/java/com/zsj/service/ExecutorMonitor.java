package com.zsj.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigService;
import com.dianping.cat.Cat;
import com.dianping.cat.util.StringUtils;
import com.zsj.config.ThreadPoolConf;
import com.zsj.util.SpringBeanUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

@Service
@Slf4j
public class ExecutorMonitor implements SmartInitializingSingleton {

    private static final String THREADS_IN_POOL = "%s_ThreadsInPool";
    private static final String ACTIVE_THREADS = "%s_ActiveThreads";
    private static final String QUEUE_SIZE_IN_POOL = "%s_QueueSizeInPool";
    private static final String IM_THREAD_POOL = "im_thread_pool";
    private static final String IM = "IM";

    private final ReentrantLock lock = new ReentrantLock();

    @Resource(name = "scheduledExecutor")
    private ScheduledExecutorService imMsgScheduledExecutor;

    @Override
    public void afterSingletonsInstantiated() {
        // 添加Apollo配置的监听器
        Config config = ConfigService.getAppConfig();
        config.addChangeListener(changeEvent -> {
            if (changeEvent.isChanged(IM_THREAD_POOL)) {
                updateExecutorConfig();
            }
        });
        // 统计线程池信息
        Runnable poolThreadTask = ()->{
            ThreadPoolTaskExecutor imMsgExecutor = (ThreadPoolTaskExecutor) SpringBeanUtil.getBean("imMsgExecutor");
            /*  the current number of threads in the pool. */
            int threadsInPool = imMsgExecutor.getPoolSize();
            /*  the number of currently active threads.*/
            int activeThreads = imMsgExecutor.getActiveCount();
            /*  the size of currently queue.*/
            int queueSize = imMsgExecutor.getThreadPoolExecutor().getQueue().size();
            Cat.logMetricForCount(String.format(THREADS_IN_POOL, IM), threadsInPool);
            Cat.logMetricForCount(String.format(ACTIVE_THREADS, IM), activeThreads);
            Cat.logMetricForCount(String.format(QUEUE_SIZE_IN_POOL, IM), queueSize);
        };
        imMsgScheduledExecutor.scheduleAtFixedRate(poolThreadTask, TimeUnit.SECONDS.toMillis(5), TimeUnit.MINUTES.toMillis(1), TimeUnit.MILLISECONDS);
    }


    private void updateExecutorConfig(){
        String contentStr = ConfigService.getAppConfig().getProperty(IM_THREAD_POOL, "{}");
        log.info("线程池信息变更，newValue={}", contentStr);
        ThreadPoolConf threadPoolConf = parseJsonString(contentStr);
        if (threadPoolConf == null) {
            return;
        }
        boolean chang = false;
        lock.lock();
        try{
            ThreadPoolTaskExecutor imMsgExecutor = (ThreadPoolTaskExecutor)SpringBeanUtil.getBean("imMsgExecutor");
            int corePoolSize = imMsgExecutor.getCorePoolSize();
            int maxPoolSize = imMsgExecutor.getMaxPoolSize();
            int configCore = threadPoolConf.getCore();
            int configMax = threadPoolConf.getMax();
            if (configCore != 0 && configCore != corePoolSize) {
                imMsgExecutor.setCorePoolSize(configCore);
                chang = true;
            }
            if (configMax != 0 && configMax != maxPoolSize) {
                imMsgExecutor.setMaxPoolSize(configMax);
                chang = true;
            }
        }finally{
            lock.unlock();
        }
        if (chang) {
            Cat.logEvent("ImMsg", "updateExecutorConfig");
        }
    }

    private ThreadPoolConf parseJsonString(String contentStr){
        try{
            if (StringUtils.isNotEmpty(contentStr)) {
                return JSON.parseObject(contentStr, new TypeReference<ThreadPoolConf>(){});
            }
        }catch(Exception e){
            log.error("parse threadPool config failed, content={}",contentStr, e);
        }
        return null;
    }
}
