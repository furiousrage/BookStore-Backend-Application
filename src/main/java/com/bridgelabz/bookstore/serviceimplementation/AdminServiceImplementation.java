package com.bridgelabz.bookstore.serviceimplementation;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bridgelabz.bookstore.exception.UserNotFoundException;
import com.bridgelabz.bookstore.model.BookModel;
import com.bridgelabz.bookstore.repository.BookRepository;
import com.bridgelabz.bookstore.repository.UserRepository;
import com.bridgelabz.bookstore.service.AdminService;
import com.bridgelabz.bookstore.utility.JwtGenerator;

@Service
public class AdminServiceImplementation implements AdminService{

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private BookRepository bookRepository;	
	
	
		
	@Override
	public List<BookModel>  getAllUnVerifiedBooks(String token) throws UserNotFoundException {
		
		long id = JwtGenerator.decodeJWT(token);
		String role = userRepository.checkRole(id);
		if(role.equals("ADMIN")){
			return bookRepository.getAllUnverfiedBooks();						
		}
		else {
			throw new UserNotFoundException("Not Authorized");
		}	
	}

}
