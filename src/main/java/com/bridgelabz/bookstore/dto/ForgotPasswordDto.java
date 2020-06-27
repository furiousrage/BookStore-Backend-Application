package com.bridgelabz.bookstore.dto;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ForgotPasswordDto {

	@NotEmpty
	@Pattern(regexp = "[a-z0-9._%+-]+@[a-z0-9.-]+\\\\.[a-z]{2,}$", message = "Please Enter Valid EmailId")
	private String  emailId;
}
