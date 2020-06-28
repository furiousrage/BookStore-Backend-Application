package com.bridgelabz.bookstore.controller;

 

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.bridgelabz.bookstore.dto.BookDto;
import com.bridgelabz.bookstore.dto.UpdateBookDto;
import com.bridgelabz.bookstore.exception.UserException;
import com.bridgelabz.bookstore.response.Response;
import com.bridgelabz.bookstore.service.SellerService;
 

import io.swagger.annotations.Api;

@RestController
@RequestMapping("/sellers")
@Api(value = "Seller Controller to perform CRUD operations on book")
public class SellerController {

	@Autowired
	private SellerService sellerService;

	@PostMapping(value = "/addBook", headers = "Accept=application/json")
	public ResponseEntity<Response> addBook(@RequestBody BookDto newBook, @RequestHeader("token") String token)
			throws UserException {
		Response addedbook = sellerService.addBook(newBook, token);
		return new ResponseEntity<Response>(addedbook, HttpStatus.OK);
	}
	
	
	@PutMapping(value = "/updateBook", headers = "Accept=application/json")
	public ResponseEntity<Response> updateBook(@RequestBody UpdateBookDto newBook, @RequestHeader("token") String token,Long bookId)
			throws UserException {
		Response addedbook = sellerService.updateBook(newBook, token, bookId);
		return new ResponseEntity<Response>(HttpStatus.OK);
	}
	
	@DeleteMapping(value = "/DeleteBook", headers = "Accept=application/json")
	public ResponseEntity<Response> deleteBook( @RequestHeader("token") String token,Long bookId)
			throws UserException {
		Response addedbook = sellerService.deleteBook(token, bookId);
		return new ResponseEntity<Response>(HttpStatus.OK);
	}
	
}
