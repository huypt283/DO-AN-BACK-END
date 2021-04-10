package com.phamthehuy.doan.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

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

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(nullable = false)
    private Integer roomPrice;

    @Column(columnDefinition = "text")
    private String description;

    @Column
    private Integer days;

    @Column
    private String roomType;

    @Column
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date expTime;

    @Column(nullable = false, columnDefinition = "BOOLEAN")
    private Boolean vip;

    @Column(nullable = false)
    private Double acreage;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false, columnDefinition = "text")
    private String images;

    private String video;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date timeUpdated;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "serviceId", nullable = false)
    private RoomService roomService;

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
