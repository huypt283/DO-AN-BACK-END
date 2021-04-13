package com.phamthehuy.doan.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Getter
@Setter
//@IdClass(FavoriteArticlesCompositeKey.class)
public class FavoriteArticle implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer favoriteId;

    @ManyToOne
    @JoinColumn(name = "articleId", nullable = false)
    private Article article;

    @ManyToOne
    @JoinColumn(name = "customerId", nullable = false)
    private Customer customer;
}
