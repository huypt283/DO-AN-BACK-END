package com.phamthehuy.doan.repository;

import com.phamthehuy.doan.entity.Staff;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface StaffRepository extends JpaRepository<Staff, Integer> {
    Page<Staff> findByNameLikeAndDeletedFalseAndEnabledTrueOrEmailLikeAndDeletedFalseAndEnabledTrueOrPhoneLikeAndDeletedFalseAndEnabledTrue(
            String name, String email, String phone, Pageable pageable
    );

    Page<Staff> findByNameLikeAndDeletedTrueAndEnabledTrueOrEmailLikeAndDeletedTrueAndEnabledTrueOrPhoneLikeAndDeletedTrueAndEnabledTrue(
            String name, String email, String phone, Pageable pageable
    );

    Page<Staff> findByNameLikeAndEnabledTrueOrEmailLikeAndEnabledTrueOrPhoneLikeAndEnabledTrue(
            String name, String email, String phone, Pageable pageable
    );

    Staff findByStaffId(Integer staffId);

    Staff findByEmail(String email);

    Staff findByToken(String token);

    Staff findByRefreshToken(String refreshToken);

    List<Staff> findByEnabledFalseAndTimeCreatedLessThanEqual(Date date);
}
