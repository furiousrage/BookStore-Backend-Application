package com.bridgelabz.bookstore.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegistrationDto {

	private String  fullName;
	
    private String  emailId;
	
	private Long mobileNumber;
	
    private String  password;
}
