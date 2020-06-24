package com.bridgelabz.bookstore.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.bridgelabz.bookstore.dto.ForgotPasswordDto;
import com.bridgelabz.bookstore.dto.LoginDto;
import com.bridgelabz.bookstore.dto.RegistrationDto;
import com.bridgelabz.bookstore.dto.ResetPasswordDto;
import com.bridgelabz.bookstore.exception.UserNotFoundException;
import com.bridgelabz.bookstore.response.Response;
import com.bridgelabz.bookstore.response.UserDetailsResponse;

@Component
public interface UserService {

	ResponseEntity<Response> register(RegistrationDto registrationDto);

	ResponseEntity<Response> verify(String token);

	ResponseEntity<Response> forgetPassword(ForgotPasswordDto emailId);

	ResponseEntity<Response> resetPassword(ResetPasswordDto resetPassword, String token) throws UserNotFoundException;

	ResponseEntity<UserDetailsResponse> login(LoginDto logindto) throws UserNotFoundException;

}
