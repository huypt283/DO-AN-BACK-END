package com.phamthehuy.doan.model.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Transaction extends BaseEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer transactionId;

    @Column(nullable = false, columnDefinition = "BOOLEAN")
    private Boolean type;

    @Column(nullable = false)
    private int amount;

    @Column(nullable = false)
    private Date time;

    @Column(nullable = true)
    private String description;

    @ManyToOne
    @JoinColumn(name = "customerId")
    private Customer customer;
}
