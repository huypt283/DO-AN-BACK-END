package com.phamthehuy.doan.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

@MappedSuperclass
@Getter
@Setter
public abstract class BaseEntity {
    @Column(nullable = false, columnDefinition = "BOOLEAN")
    private Boolean deleted = false;

    @Temporal(TemporalType.DATE)
    private Date timeCreated = new Date();

}
