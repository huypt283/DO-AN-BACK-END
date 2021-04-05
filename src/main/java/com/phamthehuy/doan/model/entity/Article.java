package com.phamthehuy.doan.model.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.Set;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Article extends BaseEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer articleId;

    @Column(nullable = false)
    private String title;

    @Column(length = 65535, columnDefinition = "text", nullable = false)
    private String content;

    @Column(nullable = false)
    private String image;

    @Column(nullable = false)
    private int roomPrice;

    @Column(length = 65535, columnDefinition = "text")
    private String description;

    @Column(nullable = false)
    private Date updateTime;

    @Column
    private Date expTime;

    @Column
    private Integer number;

    @Column
    private String type;

    @Column(nullable = false, columnDefinition = "BOOLEAN")
    private Boolean vip;

    @Column(nullable = false)
    private Integer acreage;

    @Column(nullable = false, columnDefinition = "text")
    private String address;

    private String video;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "serviceId", nullable = false)
    private Service service;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "roommateId")
    private Roommate roommate;

    @ManyToOne
    @JoinColumn(name = "customerId", nullable = false)
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "wardId", nullable = false)
    private Ward ward;

    @OneToMany(mappedBy = "article", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<FavoriteArticle> favoriteArticles;

    @OneToMany(mappedBy = "article", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<StaffArticle> staffArticles;
}
