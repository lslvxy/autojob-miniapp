package com.laisen.autojob.quartz;

import com.laisen.autojob.cloud189.service.CloudAutoCheckInService;
import com.laisen.autojob.core.constants.Constants;
import com.laisen.autojob.core.entity.EventLog;
import com.laisen.autojob.core.repository.EventLogRepository;
import com.laisen.autojob.everphoto.service.AutoCheckInService;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

@Component
public class QuartzJob extends QuartzJobBean {
    @Autowired
    AutoCheckInService      autoCheckInService;
    @Autowired
    EventLogRepository      eventLogRepository;
    @Autowired
    CloudAutoCheckInService cloudAutoCheckInService;

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        final String name = jobExecutionContext.getJobDetail().getKey().getName();
        if (name.startsWith(Constants.JOB_PREFIX_EVERPHOTO)) {
            String id = name.replace(Constants.JOB_PREFIX_EVERPHOTO, "");
            try {
                autoCheckInService.autoCheckin(id);
            } catch (Exception e) {
                EventLog l = new EventLog();
                l.setUserId(id);
                l.setDetail("签到失败," + e.getMessage());
                l.setType(Constants.LOG_EVERPHOTO);
                eventLogRepository.save(l);
            }
        } else if (name.startsWith(Constants.JOB_PREFIX_CLOUD189)) {
            String id = name.replace(Constants.JOB_PREFIX_CLOUD189, "");
            try {
                cloudAutoCheckInService.autoCheckin(id);
            } catch (Exception e) {
                EventLog l = new EventLog();
                l.setUserId(id);
                l.setDetail("签到失败," + e.getMessage());
                l.setType(Constants.LOG_CLOUD189);
                eventLogRepository.save(l);
            }
        }

    }
}
