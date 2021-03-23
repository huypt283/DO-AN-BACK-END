package com.phamthehuy.doan.dao;

import com.phamthehuy.doan.model.entity.Staff;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StaffRepository extends JpaRepository<Staff, Integer> {
    Page<Staff> findByDeletedFalse(Pageable pageable);

    List<Staff> findByDeletedFalse();

    Staff findByStaffIdAndDeletedFalse(Integer staffId);

    Page<Staff> findByNameLikeOrEmailLikeOrPhoneLikeAndDeletedFalse
            (String email, String name, String phone, Pageable pageable);

    List<Staff> findByNameLikeOrEmailLikeOrPhoneLikeAndDeletedFalse
            (String email, String name, String phone);

    List<Staff> findByDeletedTrue();
}
