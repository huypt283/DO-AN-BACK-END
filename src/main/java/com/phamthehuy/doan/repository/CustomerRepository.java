package com.phamthehuy.doan.repository;

import com.phamthehuy.doan.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Integer> {

    Customer findByEmail(String email);

    Customer findByToken(String token);

    Page<Customer> findByNameLikeAndEnabledTrueAndDeletedTrueOrPhoneLikeAndEnabledTrueAndDeletedTrueOrEmailLikeAndEnabledTrueAndDeletedTrue
            (String name, String phone, String email, Pageable pageable);

    Page<Customer> findByNameLikeAndEnabledTrueAndDeletedFalseOrPhoneLikeAndEnabledTrueAndDeletedFalseOrEmailLikeAndEnabledTrueAndDeletedFalse
            (String name, String phone, String email, Pageable pageable);

    Page<Customer> findByNameLikeAndEnabledTrueOrPhoneLikeAndEnabledTrueOrEmailLikeAndEnabledTrue
            (String name, String phone, String email, Pageable pageable);

    Customer findByCustomerIdAndDeletedFalseAndEnabledTrue(Integer id);

    List<Customer> findByDeletedTrueAndEnabledTrue();

    List<Customer> findByEnabledFalseAndTimeCreatedLessThanEqual(Date date);

    Customer findByCustomerIdAndEnabledTrue(Integer id);

    Optional<Customer> findByCustomerIdAndEnabledFalse(Integer id);
}
