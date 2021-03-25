package com.phamthehuy.doan.dao;

import com.phamthehuy.doan.model.entity.Staff;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StaffRepository extends JpaRepository<Staff, Integer> {
    List<Staff> findByDeletedFalse(Sort sort);
    List<Staff> findByDeletedFalse();

    Staff findByStaffIdAndDeletedFalse(Integer staffId);

    List<Staff> findByNameLikeAndDeletedFalse(String name);
    List<Staff> findByEmailLikeAndDeletedFalse(String name);
    List<Staff> findByPhoneLikeAndDeletedFalse(String name);

    List<Staff> findByDeletedTrue();
}
