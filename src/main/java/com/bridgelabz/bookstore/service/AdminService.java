package com.bridgelabz.bookstore.service;

import java.util.List;

import com.bridgelabz.bookstore.exception.UserNotFoundException;
import com.bridgelabz.bookstore.model.BookModel;

public interface AdminService {

	List<BookModel> getAllUnVerifiedBooks(String token) throws UserNotFoundException;


}
