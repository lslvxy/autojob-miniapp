package com.laisen.autojob.core.entity;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table
@EntityListeners(AuditingEntityListener.class)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String account;
    private String nickName;
    private String password;
    private String avatarUrl;

    @CreatedDate
    private LocalDateTime gmtCreate;

    @LastModifiedDate
    private LocalDateTime gmtModified;
}
