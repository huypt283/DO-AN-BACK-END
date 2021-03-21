package com.phamthehuy.doan.model.dto.output;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class StaffOutputDTO {
    private Integer staffId;

    private String email;

    private String pass;

    private String name;

    private String cardId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    private Date dob;

    private boolean gender;

    private boolean role;

    private String address;

    private String phone;

    private String image;
}
