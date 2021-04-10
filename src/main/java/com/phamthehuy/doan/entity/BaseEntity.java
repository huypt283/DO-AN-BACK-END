package com.phamthehuy.doan.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

@MappedSuperclass
@Getter
@Setter
public abstract class BaseEntity {
    @Column(columnDefinition = "BOOLEAN")
    private Boolean deleted = false;

    @Column(updatable = false)
    private Date timeCreated = new Date();
}
