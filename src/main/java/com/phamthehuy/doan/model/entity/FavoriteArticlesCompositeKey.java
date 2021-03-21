package com.phamthehuy.doan.model.entity;

import lombok.*;

import java.io.Serializable;

@EqualsAndHashCode
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FavoriteArticlesCompositeKey implements Serializable {
    public Customer customer;
    public Article article;
}
