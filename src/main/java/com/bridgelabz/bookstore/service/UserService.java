package com.bridgelabz.bookstore.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.bridgelabz.bookstore.dto.ForgotPasswordDto;
import com.bridgelabz.bookstore.dto.LoginDto;
import com.bridgelabz.bookstore.dto.RegistrationDto;
import com.bridgelabz.bookstore.dto.ResetPasswordDto;
import com.bridgelabz.bookstore.exception.UserException;
import com.bridgelabz.bookstore.exception.UserNotFoundException;
import com.bridgelabz.bookstore.model.UserModel;
import com.bridgelabz.bookstore.response.Response;
import com.bridgelabz.bookstore.response.UserDetailsResponse;

@Component
public interface UserService {

	boolean register(RegistrationDto registrationDto) throws UserException;

	boolean verify(String token);

	boolean forgetPassword(ForgotPasswordDto emailId);

	boolean resetPassword(ResetPasswordDto resetPassword, String token) throws UserNotFoundException;

	Response login(LoginDto logindto) throws UserNotFoundException,UserException;

	Response addToCart(Long bookId) throws UserNotFoundException;
	
	Response addMoreItems(Long bookId) throws UserNotFoundException;
	
	Response removeItem(Long bookId) throws UserNotFoundException;

}
