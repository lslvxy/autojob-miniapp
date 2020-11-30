package com.laisen.autojob.modules.everphoto.repository;

import com.laisen.autojob.modules.everphoto.entity.EverPhotoAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EverPhotoAccountRepository extends JpaRepository<EverPhotoAccount, Long> {
    EverPhotoAccount findByUserId(String userId);

}
