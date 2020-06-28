package com.bridgelabz.bookstore.service;

import com.bridgelabz.bookstore.dto.BookDto;
import com.bridgelabz.bookstore.dto.UpdateBookDto;
import com.bridgelabz.bookstore.exception.UserException;
import com.bridgelabz.bookstore.model.BookModel;
import com.bridgelabz.bookstore.response.Response;

public interface SellerService {

	Response addBook(BookDto newBook, String token) throws UserException;

	Response updateBook(UpdateBookDto newBook, String token,Long BookId) throws UserException;

	Response deleteBook(String token, Long bookId);


}
