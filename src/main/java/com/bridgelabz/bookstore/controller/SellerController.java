package com.bridgelabz.bookstore.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.bridgelabz.bookstore.dto.BookDto;
import com.bridgelabz.bookstore.dto.UpdateBookDto;
import com.bridgelabz.bookstore.exception.UserException;
import com.bridgelabz.bookstore.response.Response;
import com.bridgelabz.bookstore.service.SellerService;
import com.bridgelabz.bookstore.serviceimplementation.AmazonS3ClientServiceImpl;

import io.swagger.annotations.Api;

@RestController
@RequestMapping("/sellers")
@Api(value = "Seller Controller to perform CRUD operations on book")
public class SellerController {

	@Autowired
	private SellerService sellerService;

	@Autowired
	private Environment environment;

	@Autowired
	private AmazonS3ClientServiceImpl amazonS3Client;

	@PostMapping(value = "/addBook")
	public ResponseEntity<Response> addBook(BookDto newBook,@RequestPart("file") MultipartFile multipartFile,
			@RequestHeader("token") String token) throws UserException {
		Response addedbook = sellerService.addBook(newBook, multipartFile,token);
		return new ResponseEntity<Response>(addedbook, HttpStatus.OK);
	}

	@PostMapping(value = "/addImg", headers = "Accept=application/json")
	public ResponseEntity<Response> addimage(@RequestPart MultipartFile multipartFile) {
		String imgUrl = amazonS3Client.uploadFile(multipartFile);
		return ResponseEntity.status(HttpStatus.OK).body(new Response(HttpStatus.OK.value(), imgUrl));
	}

	@PutMapping(value = "/updateBook", headers = "Accept=application/json")
	public ResponseEntity<Response> updateBook(@RequestBody UpdateBookDto newBook, @RequestHeader("token") String token,
			Long bookId) throws UserException {
		Response addedbook = sellerService.updateBook(newBook, token, bookId);
		return new ResponseEntity<Response>(HttpStatus.OK);
	}

	@DeleteMapping(value = "/DeleteBook", headers = "Accept=application/json")
	public ResponseEntity<Response> deleteBook(@RequestHeader("token") String token, Long bookId) throws UserException {
		Response addedbook = sellerService.deleteBook(token, bookId);
		return new ResponseEntity<Response>(HttpStatus.OK);
	}

}
