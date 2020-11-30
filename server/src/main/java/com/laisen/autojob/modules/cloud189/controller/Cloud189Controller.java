package com.laisen.autojob.modules.cloud189.controller;

import com.laisen.autojob.modules.cloud189.entity.CloudAccount;
import com.laisen.autojob.modules.cloud189.repository.CloudAccountRepository;
import com.laisen.autojob.modules.cloud189.util.AESUtil;
import com.laisen.autojob.core.constants.Constants;
import com.laisen.autojob.core.controller.BaseController;
import com.laisen.autojob.core.entity.EventLog;
import com.laisen.autojob.core.repository.EventLogRepository;
import com.laisen.autojob.modules.everphoto.dto.EverPhotoJobDTO;
import com.laisen.autojob.quartz.QuartzJob;
import com.laisen.autojob.quartz.entity.QuartzBean;
import com.laisen.autojob.quartz.repository.QuartzBeanRepository;
import com.laisen.autojob.quartz.util.QuartzUtils;
import org.quartz.Scheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("cloud189")
public class Cloud189Controller extends BaseController {
    @Autowired
    private Scheduler              scheduler;
    @Autowired
    private QuartzBeanRepository   quartzBeanRepository;
    @Autowired
    private CloudAccountRepository cloudAccountRepository;
    @Autowired
    private EventLogRepository     eventLogRepository;

    @PostMapping("/create")
    @ResponseBody
    public String createJob(@RequestBody EverPhotoJobDTO dto) {
        try {
            final String userId = dto.getUserId();
            if (Objects.isNull(userId)) {
                return "用户信息不正确";
            }
            CloudAccount everPhotoAccount = cloudAccountRepository.findByUserId(userId);
            if (!Objects.isNull(everPhotoAccount)) {
                if (!everPhotoAccount.getUserId().equals(userId)) {
                    return "此账号已被其他用户绑定";
                }
            } else {
                everPhotoAccount = new CloudAccount();
            }
            everPhotoAccount.setUserId(userId);
            if (dto.getAccount().startsWith("+86")) {
                everPhotoAccount.setAccount(dto.getAccount().replace("+86", ""));
            } else {
                everPhotoAccount.setAccount(dto.getAccount());
            }
            everPhotoAccount.setPassword(AESUtil.encrypt(dto.getPassword()));
            everPhotoAccount.setTime(
                    (dto.getHour() < 10 ? "0" + dto.getHour() : dto.getHour()) + ":" + (dto.getMins() < 10 ? "0" + dto.getMins()
                            : dto.getMins()));

            cloudAccountRepository.save(everPhotoAccount);

            QuartzBean quartzBean = quartzBeanRepository.findByUserId(userId);
            if (Objects.isNull(quartzBean)) {
                quartzBean = new QuartzBean();
            }
            quartzBean.setUserId(userId);
            quartzBean.setJobClass(QuartzJob.class.getName());
            quartzBean.setJobName(Constants.JOB_PREFIX_CLOUD189 + userId);
            //"0 0 12 * * ?" 每天中午12点触发
            quartzBean.setCronExpression("0 " + dto.getMins() + " " + dto.getHour() + " * * ?");
            //            quartzBean.setCronExpression("*/10 * * * * ?");

            quartzBeanRepository.save(quartzBean);
            try {
                QuartzUtils.createScheduleJob(scheduler, quartzBean);
            } catch (Exception e) {
                QuartzUtils.updateScheduleJob(scheduler, quartzBean);
            }

            QuartzUtils.runOnce(scheduler, quartzBean.getJobName());
        } catch (Exception e) {
            e.printStackTrace();
            return "配置失败";
        }
        return "配置成功";
    }

    @PostMapping("/delete")
    @ResponseBody
    public String deleteJob(@RequestBody EverPhotoJobDTO dto) {
        try {
            QuartzBean quartzBean = quartzBeanRepository.findByUserId(dto.getUserId());
            if (!Objects.isNull(quartzBean)) {
                QuartzUtils.deleteScheduleJob(scheduler, quartzBean.getJobName());
                quartzBeanRepository.delete(quartzBean);
            }
            final CloudAccount everPhotoAccount = cloudAccountRepository.findByUserId(dto.getUserId());
            if (!Objects.isNull(everPhotoAccount)) {
                cloudAccountRepository.delete(everPhotoAccount);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "删除失败";
        }
        return "删除成功";
    }

    @PostMapping("/get")
    @ResponseBody
    public Map getDetail(@RequestBody EverPhotoJobDTO dto) {
        CloudAccount everPhotoAccount = cloudAccountRepository.findByUserId(dto.getUserId());
        Map<String, String> result = new HashMap<>();
        if (!Objects.isNull(everPhotoAccount)) {
            result.put("account", everPhotoAccount.getAccount());
            result.put("password", "******");
            result.put("time", everPhotoAccount.getTime());
        } else {
            result.put("account", "");
            result.put("password", "");
            result.put("time", "00:00");
        }
        return result;
    }

    @PostMapping("/log")
    @ResponseBody
    public List getLogs(@RequestBody EverPhotoJobDTO dto) {
        EventLog el = new EventLog();
        el.setUserId(dto.getUserId());
        el.setType(Constants.LOG_TYPE_CLOUD189);
        Example<EventLog> ex = Example.of(el);
        PageRequest page = PageRequest.of(0, 20, Sort.by(Direction.DESC, "gmtCreate"));
        List<EventLog> logs = eventLogRepository.findAll(ex, page).toList();

        return logs;
    }
}