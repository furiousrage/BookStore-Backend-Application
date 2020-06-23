package com.bridgelabz.bookstore.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.bridgelabz.bookstore.dto.RegistrationDto;
import com.bridgelabz.bookstore.response.Response;

@Component
public interface UserService {

	ResponseEntity<Response> register(RegistrationDto registrationDto);

	ResponseEntity<Response> verify(String token);

	ResponseEntity<Response> forgetPassword(RegistrationDto emailId);

	
}
