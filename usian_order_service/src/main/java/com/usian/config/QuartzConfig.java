package com.usian.config;

import com.usian.factory.MyAdaptableJobFactory;
import com.usian.quartz.OrderQuartz;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

/**
 *  Quartz配置类
 */
@Configuration
public class QuartzConfig {
    /**
     * 创建job对象：做什么事
     * @return
     */
    @Bean
    public JobDetailFactoryBean jobDetailFactoryBean(){
        JobDetailFactoryBean jobDetailFactoryBean = new JobDetailFactoryBean();
        //关联我们自己的job类
        jobDetailFactoryBean.setJobClass(OrderQuartz.class);
        return jobDetailFactoryBean;
    }

    /**
     * 创建CronTrigger对象：什么时间
     * @param jobDetailFactoryBean
     * @return
     */
    @Bean
    public CronTriggerFactoryBean cronTriggerFactoryBean(JobDetailFactoryBean jobDetailFactoryBean){
        CronTriggerFactoryBean cronTriggerFactoryBean = new CronTriggerFactoryBean();
        cronTriggerFactoryBean.setJobDetail(jobDetailFactoryBean.getObject());
        //设置触发时间
        cronTriggerFactoryBean.setCronExpression("0 */1 * * * ?");
        return cronTriggerFactoryBean;
    }

    /**
     * 创建Scheduler对象：什么时间做什么事
     * @param cronTriggerFactoryBean
     * @return
     */
    @Bean
    public SchedulerFactoryBean schedulerFactoryBean(CronTriggerFactoryBean cronTriggerFactoryBean, MyAdaptableJobFactory myAdaptableJobFactory){
        SchedulerFactoryBean schedulerFactoryBean = new SchedulerFactoryBean();
        //关联trigger
        schedulerFactoryBean.setTriggers(cronTriggerFactoryBean.getObject());
        //将OrderQuartz实例化，并添加到spring容器中
        schedulerFactoryBean.setJobFactory(myAdaptableJobFactory);
        return schedulerFactoryBean;
    }
}
