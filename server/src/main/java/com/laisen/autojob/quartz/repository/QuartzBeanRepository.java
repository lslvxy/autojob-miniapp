package com.laisen.autojob.quartz.repository;

import com.laisen.autojob.quartz.entity.QuartzBean;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuartzBeanRepository extends JpaRepository<QuartzBean, Long> {
    QuartzBean findByUserId(String userId);
}
