package com.laisen.autojob.everphoto.dto;

import lombok.Data;

@Data
public class EverPhotoJobDTO {
    private String account;
    private String password;
    private Integer timeHour = 1;
    private Integer timeMin;

}
