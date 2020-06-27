package com.bridgelabz.bookstore.serviceimplementation;

import java.time.LocalDateTime;
import java.time.LocalTime;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.bridgelabz.bookstore.dto.BookDto;
import com.bridgelabz.bookstore.exception.UserException;
import com.bridgelabz.bookstore.model.BookModel;
import com.bridgelabz.bookstore.repository.BookRepository;
import com.bridgelabz.bookstore.repository.UserRepository;
import com.bridgelabz.bookstore.response.Response;
import com.bridgelabz.bookstore.service.SellerService;
import com.bridgelabz.bookstore.utility.JwtGenerator;


@Service
public class SellerServiceImplementation implements SellerService {
	
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private BookRepository bookRepository;
	
	
	@Override
	public Response addBook(BookDto newBook, String token) throws UserException {
		long id = JwtGenerator.decodeJWT(token);
		String role = userRepository.checkRole(id);
		if(role.equals("SELLER")){
		BookModel book = new BookModel();
		BeanUtils.copyProperties(newBook, book);
		book.setVerfied(false);
		book.setUpdatedDateAndTime(LocalDateTime.now());
		book.setCreatedDateAndTime(LocalDateTime.now());
		book.isVerfied();
		bookRepository.save(book);
		return new Response(HttpStatus.OK.value(),"Book Added Successfully Need to Verify");
	     
	}
	 return new Response(HttpStatus.OK.value(),"Book Not Added Becoz Not Authoriized to add Book");
}
	
	
	
	
	
}
