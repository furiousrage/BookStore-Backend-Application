package com.bridgelabz.bookstore.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bridgelabz.bookstore.dto.ForgotPasswordDto;
import com.bridgelabz.bookstore.dto.LoginDto;
import com.bridgelabz.bookstore.dto.RegistrationDto;
import com.bridgelabz.bookstore.dto.ResetPasswordDto;
import com.bridgelabz.bookstore.exception.UserException;
import com.bridgelabz.bookstore.exception.UserNotFoundException;
import com.bridgelabz.bookstore.model.BookModel;
import com.bridgelabz.bookstore.response.Response;
import com.bridgelabz.bookstore.service.ElasticSearchService;
import com.bridgelabz.bookstore.service.UserService;

import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/user")
@CrossOrigin(allowedHeaders = "*", origins = "*")
@PropertySource(name = "user", value = { "classpath:response.properties" })
public class UserController {

	@Autowired
	private UserService userService;

	@Autowired
	private Environment environment;

	@Autowired
	private ElasticSearchService elasticSearchService;

	@PostMapping("/register")
	public ResponseEntity<Response> register(@RequestBody @Valid RegistrationDto registrationDto, BindingResult result)
			throws UserException {

		if (result.hasErrors())
			return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(new Response(
					result.getAllErrors().get(0).getDefaultMessage(), HttpStatus.OK.value(), "Invalid Credentials"));

		if (userService.register(registrationDto))
			return ResponseEntity.status(HttpStatus.OK)
					.body(new Response(HttpStatus.OK.value(), environment.getProperty("user.register.successfull")));

		return ResponseEntity.status(HttpStatus.OK)
				.body(new Response(HttpStatus.BAD_REQUEST.value(), "Sorry! Failed "));
	}

	@GetMapping("/verify/{token}")
	public ResponseEntity<Response> userVerification(@PathVariable("token") String token) {

		if (userService.verify(token))
			return ResponseEntity.status(HttpStatus.OK)
					.body(new Response(HttpStatus.OK.value(), "Verified Successfully"));

		return ResponseEntity.status(HttpStatus.OK).body(new Response(HttpStatus.BAD_REQUEST.value(), "Not Verified"));
	}

	@PostMapping("/forgotpassword")
	public ResponseEntity<Response> forgotPassword(@RequestBody @Valid ForgotPasswordDto emailId) {

		if (userService.forgetPassword(emailId))
			return ResponseEntity.status(HttpStatus.OK)
					.body(new Response(HttpStatus.OK.value(), "Password is send to the Email-Id"));

		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(new Response(HttpStatus.BAD_REQUEST.value(), "Sorry!! User Doesn't Exist"));
	}

	@PutMapping("/resetpassword/{token}")
	public ResponseEntity<Response> resetPassword(@RequestBody @Valid ResetPasswordDto resetPassword,
			@PathVariable("token") String token) throws UserNotFoundException {

		if (userService.resetPassword(resetPassword, token))
			return ResponseEntity.status(HttpStatus.OK)
					.body(new Response(HttpStatus.OK.value(), "Password is Update Successfully"));

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(HttpStatus.BAD_REQUEST.value(),
				"Password and Confirm Password doesn't matched please enter again"));
	}

	@ApiOperation(value = "To login")
	@PostMapping("/login")
	@CrossOrigin(origins = "http://localhost:4200")
	public ResponseEntity<Response> login(@RequestBody LoginDto loginDTO) throws UserNotFoundException, UserException {
		Response response = userService.login(loginDTO);
		return new ResponseEntity<Response>(response, HttpStatus.OK);

	}

	@ApiOperation(value = "Add Books to Cart")
	@PostMapping("/AddToCart")
	@CrossOrigin(origins = "http://localhost:4200")
	public ResponseEntity<Response> AddToCart(@RequestParam Long bookId) throws UserNotFoundException {
		Response response = userService.addToCart(bookId);

		return new ResponseEntity<Response>(response, HttpStatus.OK);

	}

	@ApiOperation(value = "Adding More Items To Cart")
	@PostMapping("/addMoreItems")
	@CrossOrigin(origins = "http://localhost:4200")
	public ResponseEntity<Response> addMoreItems(@RequestParam Long bookId)
			throws UserNotFoundException, UserException {
		Response response = userService.addMoreItems(bookId);
		return new ResponseEntity<Response>(response, HttpStatus.OK);

	}

	@ApiOperation(value = "Remove Items from Cart")
	@PostMapping("/removeFromCart")
	@CrossOrigin(origins = "http://localhost:4200")
	public ResponseEntity<Response> removeFromCart(@RequestParam Long bookId)
			throws UserNotFoundException, UserException {
		Response response = userService.removeItem(bookId);
		return new ResponseEntity<Response>(response, HttpStatus.OK);

	}
	
	@ApiOperation(value = "Add Book to Elastic Search")
	@PostMapping("/search")
	@CrossOrigin(origins = "http://localhost:3000")
	public List<BookModel> search(@RequestParam String searchItem) {
	return elasticSearchService.searchByTitle(searchItem);
	}
	

}