package com.phamthehuy.doan.model.dto.input;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SignupDTO {
    private String name;
    private Boolean gender;
    private String email;
    private String pass;
    private String phone;
}
