package com.bridgelabz.bookstore.service;

import com.bridgelabz.bookstore.exception.BookException;
import com.bridgelabz.bookstore.model.BookModel;
import com.bridgelabz.bookstore.model.CartModel;
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

import java.util.List;

@Component
public interface UserService {

	boolean register(RegistrationDto registrationDto) throws UserException;

	boolean verify(String token);

	UserDetailsResponse forgetPassword(ForgotPasswordDto emailId);

	boolean resetPassword(ResetPasswordDto resetPassword, String token) throws UserNotFoundException;

	Response login(LoginDto logindto) throws UserNotFoundException, UserException;

	Response addToCart(Long bookId) throws BookException;

	Response addMoreItems(Long bookId) throws BookException;

	Response removeItem(Long bookId) throws BookException;

	Response removeAllItem();

	List<CartModel> getAllItemFromCart() throws BookException;

	List<BookModel> sortBookByAsc();

	List<BookModel> sortBookByDesc();
	
	
	List<BookModel> getAllBooks() throws UserException;
	BookModel getBookDetails(Long bookId) throws UserException;

}
