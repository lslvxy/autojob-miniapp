package com.laisen.autojob.modules.everphoto.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table
public class EverPhotoAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private Date gmtCreate;

    @UpdateTimestamp
    private Date gmtModified;

    private String userId;

    private String account;

    private String password;
    private String time;

}
