package com.bridgelabz.bookstore.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.bridgelabz.bookstore.dto.RegistrationDto;
import com.bridgelabz.bookstore.response.Response;
import com.bridgelabz.bookstore.service.UserService;

@RestController
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	private UserService userService;
	
	
	@PostMapping("/register")
	public ResponseEntity<Response> register(@RequestBody RegistrationDto registrationDto) {		
	
		return userService.register(registrationDto);
	}  
	
	@GetMapping("/verify/{token}")
	public ResponseEntity<Response> userVerification(@PathVariable("token") String token)  {
	    
		return userService.verify(token);
	}
	
}