package com.bridgelabz.bookstore.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bridgelabz.bookstore.dto.BookDto;
import com.bridgelabz.bookstore.exception.UserException;
import com.bridgelabz.bookstore.model.BookModel;
import com.bridgelabz.bookstore.response.Response;
import com.bridgelabz.bookstore.service.SellerService;
import com.bridgelabz.bookstore.utility.Utils;

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
}
