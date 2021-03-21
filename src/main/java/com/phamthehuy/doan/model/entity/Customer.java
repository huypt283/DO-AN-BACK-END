package com.phamthehuy.doan.model.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
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

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private String cardId;

    @Column(nullable = false)
    private int accountBalance;

    @OneToMany(mappedBy = "customer", fetch = FetchType.EAGER)
    private Set<FavoriteArticle> favoriteArticles;

    @OneToMany(mappedBy = "customer", fetch = FetchType.EAGER)
    private Set<Article> articles;

    @OneToMany(mappedBy = "customer", fetch = FetchType.EAGER)
    private Set<Transaction> transactions;
}
