package com.laisen.autojob.modules.everphoto.dto;

import lombok.Data;

@Data
public class EverPhotoJobDTO {

    private String  userId;
    private String  account;
    private String  password;
    private Integer hour = 0;
    private Integer mins = 0;

}
