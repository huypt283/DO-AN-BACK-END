package com.phamthehuy.doan.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TransactionStatistic {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer transactionStatisticId;

    @Column(nullable = false, unique = true)
    private String status;

    @Column(nullable = false, columnDefinition = "INT default 0")
    private Integer count;
}
