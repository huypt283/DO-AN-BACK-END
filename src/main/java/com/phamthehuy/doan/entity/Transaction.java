package com.phamthehuy.doan.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

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
    private Integer amount;

    @Column
    private String description;

    @ManyToOne
    @JoinColumn(name = "customerId", nullable = false)
    private Customer customer;
}
