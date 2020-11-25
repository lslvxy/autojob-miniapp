package com.laisen.autojob.everphoto.repository;

import com.laisen.autojob.everphoto.entity.EverPhotoAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EverPhotoAccountRepository extends JpaRepository<EverPhotoAccount, Long> {
    EverPhotoAccount findByUserId(String userId);

}
