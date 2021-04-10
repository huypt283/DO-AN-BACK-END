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
public class Staff extends BaseEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer staffId;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String pass;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String cardId;

    @Column(nullable = false)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date dob;

    @Column(nullable = false, columnDefinition = "BOOLEAN")
    private Boolean gender;

    @Column(nullable = false, columnDefinition = "BOOLEAN")
    private Boolean role;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false, columnDefinition = "text", length = 2000)
    private String image;

    @Column(nullable = false, columnDefinition = "BOOLEAN")
    private Boolean enabled = false;

    @Column(nullable = true, unique = true)
    private String token;

    @Column
    private String refreshToken;

//    @OneToMany(mappedBy = "staff", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
//    private Set<News> news;
//
//    @OneToMany(mappedBy = "staff", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
//    private Set<StaffArticle> staffArticles;
}
