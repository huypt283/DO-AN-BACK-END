package com.phamthehuy.doan.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Getter
@Setter
public class StaffArticle implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer staffArticleId;

    @ManyToOne
    @JoinColumn(name = "staffId", nullable = false)
    private Staff staff;

    @ManyToOne
    @JoinColumn(name = "articleId", nullable = false)
    private Article article;

    @Column(nullable = false)
    private Date time = new Date();

    @Column(nullable = false)
    private String action;
}
