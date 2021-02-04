package com.zsj.util;
import com.google.common.collect.Lists;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class SpringBeanUtil implements ApplicationContextAware{

    private static ApplicationContext applicationContext;

    @Override
    public synchronized void setApplicationContext(ApplicationContext applicationContextParam) throws BeansException {
        if (applicationContext == null) {
            SpringBeanUtil.applicationContext = applicationContextParam;
        }
    }

    public static Object getBean(String beanName) {
        return applicationContext.getBean(beanName);
    }

    public static <T> List<T> getAllBeans(Class<T> clazz) throws NoSuchBeanDefinitionException {
        Map<String, T> map = applicationContext.getBeansOfType(clazz);
        if (map == null || map.isEmpty()) {
            return Lists.newArrayList();
        }
        List<T> list = new ArrayList<>(map.size());
        map.forEach((key, value) -> list.add(value));
        return list;
    }

}
