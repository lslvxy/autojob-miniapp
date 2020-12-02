package com.laisen.autojob.quartz;

import com.laisen.autojob.core.constants.Constants;
import com.laisen.autojob.core.entity.EventLog;
import com.laisen.autojob.core.repository.EventLogRepository;
import com.laisen.autojob.core.service.MessageService;
import com.laisen.autojob.core.utils.LogUtils;
import com.laisen.autojob.modules.cloud189.service.CloudAutoCheckInService;
import com.laisen.autojob.modules.everphoto.service.AutoCheckInService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class QuartzJob extends QuartzJobBean {
    @Autowired
    AutoCheckInService autoCheckInService;
    @Autowired
    EventLogRepository eventLogRepository;
    @Autowired
    CloudAutoCheckInService cloudAutoCheckInService;
    @Autowired
    MessageService messageService;

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        final String name = jobExecutionContext.getJobDetail().getKey().getName();
        LogUtils.info(log, Constants.LOG_MODULES_CORE, Constants.LOG_OPERATE_JOB, "执行Job:{}", name);

        if (name.startsWith(Constants.JOB_PREFIX_EVERPHOTO)) {
            String id = name.replace(Constants.JOB_PREFIX_EVERPHOTO, "");
            try {
                autoCheckInService.autoCheckin(id);
            } catch (Exception e) {
                EventLog l = new EventLog();
                l.setUserId(id);
                String detail = "签到失败," + e.getMessage();
                l.setDetail(detail.length() > 255 ? detail.substring(0, 250) : detail);
                l.setType(Constants.LOG_TYPE_EVERPHOTO);
                eventLogRepository.save(l);
                messageService.sendMessage(id, "时光相册签到", detail);
                LogUtils.info(log, id, Constants.LOG_MODULES_EVERPHOTO, Constants.LOG_OPERATE_CHECKIN, detail);
            }
        } else if (name.startsWith(Constants.JOB_PREFIX_CLOUD189)) {
            String id = name.replace(Constants.JOB_PREFIX_CLOUD189, "");
            try {
                cloudAutoCheckInService.autoCheckin(id);
            } catch (Exception e) {
                EventLog l = new EventLog();
                l.setUserId(id);
                String detail = "签到失败," + e.getMessage();
                l.setDetail(detail.length() > 255 ? detail.substring(0, 250) : detail);
                l.setType(Constants.LOG_TYPE_CLOUD189);
                eventLogRepository.save(l);
                messageService.sendMessage(id, "天翼网盘签到", detail);
                LogUtils.info(log, id, Constants.LOG_MODULES_CLOUD189, Constants.LOG_OPERATE_CHECKIN, detail);
            }
        }

    }
}
