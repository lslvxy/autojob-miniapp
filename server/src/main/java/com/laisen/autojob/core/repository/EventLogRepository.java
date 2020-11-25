package com.laisen.autojob.core.repository;

import com.laisen.autojob.core.entity.EventLog;
import com.laisen.autojob.core.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventLogRepository extends JpaRepository<EventLog, Long> {
    EventLog findByUserId(String userId);
}
