package com.laisen.autojob.modules.cloud189.repository;

import com.laisen.autojob.modules.cloud189.entity.CloudAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CloudAccountRepository extends JpaRepository<CloudAccount, Long> {
    CloudAccount findByUserId(String userId);

}
