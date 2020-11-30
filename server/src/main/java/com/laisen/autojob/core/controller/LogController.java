package com.laisen.autojob.core.controller;

import com.alibaba.fastjson.JSON;
import com.laisen.autojob.core.entity.EventLog;
import com.laisen.autojob.core.repository.EventLogRepository;
import com.laisen.autojob.core.service.dto.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("logs")
public class LogController extends BaseController {

    @Autowired
    EventLogRepository eventLogRepository;

    @RequestMapping("view")
    public ModelAndView login(ModelAndView modelAndView, HttpServletRequest request) {
        final String userId = super.getUserIdByRequest(request);
        EventLog el = new EventLog();
        el.setUserId(userId);
        Example<EventLog> ex = Example.of(el);
        List<EventLog> logs = eventLogRepository.findAll(ex);

        ModelAndView logspage = new ModelAndView("logspage");
        logspage.addObject("logs", logs);
        return logspage;
    }

    @RequestMapping("send")
    public String send(Message dto) {
        String s = JSON.toJSONString(dto);
        System.out.println(s);
        return s;
    }
}
