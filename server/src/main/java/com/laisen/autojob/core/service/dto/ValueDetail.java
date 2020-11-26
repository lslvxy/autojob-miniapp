/*
 * Alipay.com Inc.
 * Copyright (c) 2004-2020 All Rights Reserved.
 */
package com.laisen.autojob.core.service.dto;

import lombok.Data;

/**
 * @author lise
 * @version ValueDetail.java, v 0.1 2020年11月26日 14:38 lise
 */
@Data
public class ValueDetail {
    private String value;
    private String DATA;

    public ValueDetail(String value) {
        this.value = value;
        this.DATA = value;
    }
}