package com.phamthehuy.doan.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Getter
@Setter
@IdClass(FavoriteArticlesCompositeKey.class)
public class FavoriteArticle extends BaseEntity implements Serializable {
    @Id
    @ManyToOne
    @JoinColumn(name = "articleId")
    private Article article;

    @Id
    @ManyToOne
    @JoinColumn(name = "customerId")
    private Customer customer;
}
