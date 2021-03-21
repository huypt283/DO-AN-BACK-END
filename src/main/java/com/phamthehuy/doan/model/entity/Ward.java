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
public class Ward extends BaseEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer wardId;

    @Column(nullable = false)
    private String wardName;

    @ManyToOne
    @JoinColumn(name = "districtId")
    private District district;

    @OneToMany(mappedBy = "ward", fetch = FetchType.EAGER)
    private Set<Article> articles;
}
