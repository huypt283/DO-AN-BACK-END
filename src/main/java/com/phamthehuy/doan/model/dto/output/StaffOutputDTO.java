package com.phamthehuy.doan.model.dto.output;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StaffOutputDTO {
    private Integer staffId;

    private String email;

    private String pass;

    private String name;

    private String cardId;

    private Long dob;

    private Boolean gender;

    private Boolean role;

    private String address;

    private String phone;

    private String image;
}
