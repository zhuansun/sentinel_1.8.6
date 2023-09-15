package com.zspc.sentinel.config;

import com.zspc.sentinel.config.pool.AsyncSendMessagePool;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;


@Component
public class ApplicationContextProvider implements ApplicationContextAware, BeanPostProcessor {
    /**
     * 上下文对象实例
     */
    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ApplicationContextProvider.applicationContext = applicationContext;
    }

    /**
     * 获取applicationContext
     *
     * @return
     */
    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    /**
     * 通过class获取Bean.
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T getBean(Class<T> clazz) {
        try {
            return getApplicationContext().getBean(clazz);
        } catch (NoSuchBeanDefinitionException e) {
            return null;
        }
    }

    /**
     * 通过class以及构造参数获取Bean.
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T getBean(Class<T> clazz, Object arg1) {
        return getApplicationContext().getBean(clazz, arg1);
    }

    /**
     * 通过name获取 Bean.
     *
     * @param name
     * @return
     */
    public static Object getBean(String name) {
        try {
            return getApplicationContext().getBean(name);
        } catch (NoSuchBeanDefinitionException e) {
            return null;
        }
    }

    /**
     * 通过name,以及Clazz返回指定的Bean
     *
     * @param name
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T getBean(String name, Class<T> clazz) {
        try {
            return getApplicationContext().getBean(name, clazz);
        } catch (NoSuchBeanDefinitionException e) {
            return null;
        }
    }

    @Override
    public Object postProcessBeforeInitialization(Object o, String s) throws BeansException {
        return o;
    }

    @Override
    public Object postProcessAfterInitialization(Object o, String s) throws BeansException {
        return o;
    }

    @PreDestroy
    public void shutDown() {
        AsyncSendMessagePool.shutDown();
    }
}
