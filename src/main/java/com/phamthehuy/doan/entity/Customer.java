package com.phamthehuy.doan.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
public class Customer extends BaseEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer customerId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, columnDefinition = "BOOLEAN")
    private Boolean gender;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String pass;

    @Column(length = 65535, columnDefinition = "text")
    private String address;

    @Column(nullable = false)
    private String phone;

    @Column
    private String cardId;

    @Column
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date dob;

    @Column(nullable = false)
    private Integer accountBalance;

    @Column(nullable = false, columnDefinition = "BOOLEAN")
    private Boolean enabled;

    @Column
    private String token;

    @Column
    private String refreshToken;

    @Column(columnDefinition = "text", length = 2000)
    private String image;

    @JsonIgnore
    @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<FavoriteArticle> favoriteArticles;

//    @JsonIgnore
//    @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
//    private Set<Article> articles;
//
//    @JsonIgnore
//    @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
//    private Set<Transaction> transactions;
}
