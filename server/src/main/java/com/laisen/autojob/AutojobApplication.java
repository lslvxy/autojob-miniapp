package com.laisen.autojob;

import com.laisen.autojob.modules.cloud189.service.CloudAutoCheckInService;
import com.laisen.autojob.modules.everphoto.repository.EverPhotoAccountRepository;
import com.laisen.autojob.modules.everphoto.service.AutoCheckInService;
import com.laisen.autojob.quartz.entity.QuartzBean;
import com.laisen.autojob.quartz.repository.QuartzBeanRepository;
import com.laisen.autojob.quartz.util.QuartzUtils;
import org.quartz.Scheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.List;

@SpringBootApplication
@EnableJpaAuditing
public class AutojobApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(AutojobApplication.class, args);
    }

    @Autowired
    AutoCheckInService         autoCheckInService;
    @Autowired
    EverPhotoAccountRepository everPhotoAccountRepository;
    @Autowired
    private Scheduler               scheduler;
    @Autowired
    private QuartzBeanRepository    quartzBeanRepository;
    @Autowired
    private CloudAutoCheckInService cloudAutoCheckInService;

    @Override
    public void run(String... args) {

        List<QuartzBean> all = quartzBeanRepository.findAll();
        all.forEach(v -> {
            try {
                QuartzUtils.createScheduleJob(scheduler, v);
            } catch (Exception e) {
                QuartzUtils.updateScheduleJob(scheduler, v);
            }
           // QuartzUtils.runOnce(scheduler, v.getJobName());
        });
    }
}
