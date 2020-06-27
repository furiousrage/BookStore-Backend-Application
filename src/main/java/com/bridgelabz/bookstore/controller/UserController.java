package com.bridgelabz.bookstore.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bridgelabz.bookstore.dto.ForgotPasswordDto;
import com.bridgelabz.bookstore.dto.LoginDto;
import com.bridgelabz.bookstore.dto.RegistrationDto;
import com.bridgelabz.bookstore.dto.ResetPasswordDto;
import com.bridgelabz.bookstore.exception.UserException;
import com.bridgelabz.bookstore.exception.UserNotFoundException;
import com.bridgelabz.bookstore.response.Response;
import com.bridgelabz.bookstore.service.UserService;
import com.bridgelabz.bookstore.utility.Utils;

import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/user")
@CrossOrigin(allowedHeaders = "*", origins = "*")
public class UserController {

	@Autowired
	private UserService userService;

	@PostMapping("/register")
	public ResponseEntity<Response> register(@RequestBody @Valid RegistrationDto registrationDto, BindingResult result)
			throws UserException {

		if (result.hasErrors())
			return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(new Response(
					result.getAllErrors().get(0).getDefaultMessage(), Utils.OK_RESPONSE_CODE, "Invalid Credentials"));

		if (userService.register(registrationDto))
			return ResponseEntity.status(HttpStatus.OK)
					.body(new Response(Utils.OK_RESPONSE_CODE, "Registration Successfull"));

		return ResponseEntity.status(HttpStatus.OK)
				.body(new Response(Utils.BAD_REQUEST_RESPONSE_CODE, "Sorry! Failed to Register"));
	}

	@GetMapping("/verify/{token}")
	public ResponseEntity<Response> userVerification(@PathVariable("token") String token) {

		if (userService.verify(token))
			return ResponseEntity.status(HttpStatus.OK)
					.body(new Response(Utils.OK_RESPONSE_CODE, "Verified Successfully"));

		return ResponseEntity.status(HttpStatus.OK).body(new Response(Utils.BAD_REQUEST_RESPONSE_CODE, "Not Verified"));
	}

	@PostMapping("/forgotpassword")
	public ResponseEntity<Response> forgotPassword(@RequestBody @Valid ForgotPasswordDto emailId) {

		if (userService.forgetPassword(emailId))
			return ResponseEntity.status(HttpStatus.OK)
					.body(new Response(Utils.OK_RESPONSE_CODE, "Password is send to the Email-Id"));

		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(new Response(Utils.BAD_REQUEST_RESPONSE_CODE, "Sorry!! User Doesn't Exist"));
	}

	@PutMapping("/resetpassword/{token}")
	public ResponseEntity<Response> resetPassword(@RequestBody @Valid ResetPasswordDto resetPassword,
			@PathVariable("token") String token) throws UserNotFoundException {

		if (userService.resetPassword(resetPassword, token))
			return ResponseEntity.status(HttpStatus.OK)
					.body(new Response(Utils.OK_RESPONSE_CODE, "Password is Update Successfully"));

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(Utils.BAD_REQUEST_RESPONSE_CODE,
				"Password and Confirm Password doesn't matched please enter again"));
	}

	@ApiOperation(value = "To login")
	@PostMapping("/login")
	@CrossOrigin(origins = "http://localhost:4200")
	public ResponseEntity<Response> login(@RequestBody LoginDto loginDTO) throws UserNotFoundException, UserException {
		Response response = userService.login(loginDTO);
		return new ResponseEntity<Response>(response, HttpStatus.OK);

	}
}