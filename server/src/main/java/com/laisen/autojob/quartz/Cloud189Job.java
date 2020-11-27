package com.laisen.autojob.quartz;

import com.laisen.autojob.cloud189.service.CloudAutoCheckInService;
import com.laisen.autojob.core.entity.EventLog;
import com.laisen.autojob.core.repository.EventLogRepository;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

@Component
public class Cloud189Job extends QuartzJobBean {
    @Autowired
    CloudAutoCheckInService cloudAutoCheckInService;
    @Autowired
    EventLogRepository      eventLogRepository;

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        final String name = jobExecutionContext.getJobDetail().getKey().getName();
        String id = name.replace("cloud189.job.", "");
        try {
            cloudAutoCheckInService.autoCheckin(id);
        } catch (Exception e) {
            EventLog l = new EventLog();
            l.setUserId(id);
            l.setDetail("签到失败," + e.getMessage());
            l.setType("cloud189");
            eventLogRepository.save(l);
            e.printStackTrace();
        }
    }
}
