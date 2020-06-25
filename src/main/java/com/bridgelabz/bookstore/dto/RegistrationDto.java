package com.bridgelabz.bookstore.dto;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import lombok.Data;

@Data
public class RegistrationDto {
	
	@NotBlank(message = "please enter FullName")
	@Size(min = 3)
	@Pattern(regexp = "^[A-Z][a-z]+\\s?[A-Z][a-z]+$", message = "Please Enter Valid FullName")
	private String  fullName;
	
	
	@Email
    private String  emailId;
    
	@Pattern(regexp="(\\5|6|7|8|9) [0-9] {9}", message ="please enter valid mobile number")
	private String mobileNumber;
	
	@NotBlank
	@Size(min = 8)
	@Pattern(regexp = "(?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{8,}", message = "length should be 8 must contain atleast one uppercase, lowercase, special character and number")
    private String  password;
    
//    public static Boolean isValid(Long mobileNumber) {
//		String phoneNo = Long.toString(mobileNumber);
//		java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("^[6-9][0-9]{9}$");
//		Matcher match = pattern.matcher(phoneNo);
//		return match.find();
//	}
}
