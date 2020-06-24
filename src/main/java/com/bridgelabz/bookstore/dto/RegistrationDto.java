package com.bridgelabz.bookstore.dto;

import java.util.regex.Matcher;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegistrationDto {
	
	@NotEmpty(message = "Enter FullName - Registration DTO")
	@Size(min = 3)
	@Pattern(regexp = "^[A-Z][a-z]+\\s?[A-Z][a-z]+$", message = "Please Enter Valid FullName")
	private String  fullName;
	
	@NotEmpty
	@Pattern(regexp = "[a-z0-9._%+-]+@[a-z0-9.-]+\\\\.[a-z]{2,}$", message = "Please Enter Valid EmailId")
    private String  emailId;
	
	@NotEmpty
	private Long mobileNumber;
	
	@NotEmpty
	@Size(min = 3)
	@Pattern(regexp = "(?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{8,}", message = "length should be 8 must contain atleast one uppercase, lowercase, special character and number")
    private String  password;
    
    public static Boolean isValid(Long mobileNumber) {
		String phoneNo = Long.toString(mobileNumber);
		java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("^[6-9][0-9]{9}$");
		Matcher match = pattern.matcher(phoneNo);
		return match.find();
	}
}
