package com.laisen.autojob.modules.cloud189.dto;

import lombok.Data;

@Data
public class Cloud189JobDTO {

    private String  userId;
    private String  account;
    private String  password;
    private Integer hour = 0;
    private Integer mins = 0;

}
