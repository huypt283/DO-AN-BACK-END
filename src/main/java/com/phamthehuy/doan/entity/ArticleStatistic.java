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
public class ArticleStatistic {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer articleStatisticId;

    @Column(nullable = false, unique = true)
    private String time;

    @Column(nullable = false, columnDefinition = "INT default 0")
    private Integer count;
}
