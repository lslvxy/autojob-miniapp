package com.laisen.autojob.core.controller;

import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;

public class BaseController {

    protected String getUserIdByRequest(HttpServletRequest request) {
        final String userId = request.getSession().getAttribute("userId").toString();
        if (StringUtils.isEmpty(userId)) {
            return null;
        }
        return userId;
    }
}
