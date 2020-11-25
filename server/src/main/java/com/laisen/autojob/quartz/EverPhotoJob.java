package com.laisen.autojob.quartz;

import com.laisen.autojob.everphoto.service.AutoCheckInService;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

@Component
public class EverPhotoJob extends QuartzJobBean {
    @Autowired
    AutoCheckInService autoCheckInService;

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        final String name = jobExecutionContext.getJobDetail().getKey().getName();
        try {
            autoCheckInService.autoCheckin((name.replace("everphoto.job.", "")));
        } catch (Exception e) {

        }
    }
}
