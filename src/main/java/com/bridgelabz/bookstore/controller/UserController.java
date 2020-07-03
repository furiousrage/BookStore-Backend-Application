package com.bridgelabz.bookstore.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import com.bridgelabz.bookstore.exception.BookException;
import com.bridgelabz.bookstore.model.CartModel;
import com.bridgelabz.bookstore.model.UserModel;

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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.*;

import com.bridgelabz.bookstore.dto.ForgotPasswordDto;
import com.bridgelabz.bookstore.dto.LoginDto;
import com.bridgelabz.bookstore.dto.RegistrationDto;
import com.bridgelabz.bookstore.dto.ResetPasswordDto;
import com.bridgelabz.bookstore.exception.UserException;
import com.bridgelabz.bookstore.exception.UserNotFoundException;
import com.bridgelabz.bookstore.model.BookModel;
import com.bridgelabz.bookstore.response.Response;
import com.bridgelabz.bookstore.service.AmazonS3ClientService;
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
	
	@Autowired
     private AmazonS3ClientService amazonS3ClientService;
     
	
	@PostMapping("/register")
	public ResponseEntity<Response> register(@RequestBody @Valid  RegistrationDto registrationDto, BindingResult result)throws UserException {

		if (result.hasErrors())
			return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(new Response(result.getAllErrors().get(0).getDefaultMessage(), HttpStatus.OK.value(), "Invalid Credentials"));

		if (userService.register(registrationDto))
			return ResponseEntity.status(HttpStatus.OK)
					.body(new Response(HttpStatus.OK.value(), environment.getProperty("user.register.successfull")));

		return ResponseEntity.status(HttpStatus.OK)
				.body(new Response(HttpStatus.BAD_REQUEST.value(), environment.getProperty("user.register.unsuccessfull")));
	}

	@GetMapping("/verify/{token}")
	public ResponseEntity<Response> userVerification(@PathVariable("token") String token) {

		if (userService.verify(token))
			return ResponseEntity.status(HttpStatus.OK)
					.body(new Response(HttpStatus.OK.value(), environment.getProperty("user.verified.successfull")));

		return ResponseEntity.status(HttpStatus.OK).body(new Response(HttpStatus.BAD_REQUEST.value(), environment.getProperty("user.verified.unsuccessfull")));
	}

	@PostMapping("/forgotpassword")
	public ResponseEntity<Response> forgotPassword(@RequestBody @Valid ForgotPasswordDto emailId) {

		if (userService.forgetPassword(emailId))
			return ResponseEntity.status(HttpStatus.OK)
					.body(new Response(HttpStatus.OK.value(), environment.getProperty("user.forgotpassword.successfull")));

		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(new Response(HttpStatus.BAD_REQUEST.value(), environment.getProperty("user.forgotpassword.failed")));
	}
	
	@PutMapping("/resetpassword/{token}")
	public ResponseEntity<Response> resetPassword(@RequestBody @Valid ResetPasswordDto resetPassword,
			@PathVariable("token") String token) throws UserNotFoundException {

		if (userService.resetPassword(resetPassword, token))
			return ResponseEntity.status(HttpStatus.OK)
					.body(new Response(HttpStatus.OK.value(), environment.getProperty("user.resetpassword.successfull")));

		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(new Response(HttpStatus.BAD_REQUEST.value(), environment.getProperty("user.resetpassword.failed")));
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
	public ResponseEntity<Response> AddToCart(@RequestHeader String token, @RequestParam Long bookId)
			throws BookException, UserNotFoundException {
		Response response = userService.addToCart(token, bookId);
		
		return new ResponseEntity<Response>(response, HttpStatus.OK);

	}

	@ApiOperation(value = "Adding More Items To Cart")
	@PostMapping("/addMoreItems")
	@CrossOrigin(origins = "http://localhost:4200")
	public ResponseEntity<Response> addMoreItems(@RequestParam Long bookId) throws BookException {
		Response response = userService.addMoreItems(bookId);
		return new ResponseEntity<Response>(response, HttpStatus.OK);
	}

	@ApiOperation(value = "Remove Items from Cart")
	@PostMapping("/removeFromCart")
	@CrossOrigin(origins = "http://localhost:4200")
	public ResponseEntity<Response> removeFromCart(@RequestParam Long bookId) throws BookException {
		Response response = userService.removeItem(bookId);
		return new ResponseEntity<Response>(response, HttpStatus.OK);
	}

	@ApiOperation(value = "Remove All Items from Cart")
	@DeleteMapping("/removeAllFromCart")
	@CrossOrigin(origins = "http://localhost:4200")
	public ResponseEntity<Response> removeAllFromCart() {
		Response response = userService.removeAllItem();
		return new ResponseEntity<Response>(response, HttpStatus.OK);
	}

	@ApiOperation(value = "Get All Items from Cart")
	@GetMapping("/getAllFromCart")
	@CrossOrigin(origins = "http://localhost:4200")
	public List<CartModel> getAllItemsFromCart() throws BookException {
		return userService.getAllItemFromCart();
	}

	@ApiOperation(value = "Add Book to Elastic Search")
	@PostMapping("/search")
	@CrossOrigin(origins = "http://localhost:3000")
	public List<BookModel> search(@RequestParam String searchItem) {
		return elasticSearchService.searchByTitle(searchItem);
	}
	
	@GetMapping("/getBooksByPriceAsc")
	public ResponseEntity<Response> sortBookByPriceAsc(){
		List<BookModel> sortBookByPriceAsc = userService.sortBookByAsc();
		if(!sortBookByPriceAsc.isEmpty()) 
			return ResponseEntity.status(HttpStatus.OK)
					.body(new Response( environment.getProperty("user.bookdisplayed.lowtohigh"), HttpStatus.OK.value(), sortBookByPriceAsc));
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(new Response(HttpStatus.NOT_FOUND.value(), environment.getProperty("user.bookdisplayed.failed")));
	}
	
	@GetMapping("/getBooksByPriceDesc")
	public ResponseEntity<Response> sortBookByPriceDesc(){
		List<BookModel> sortBookByPriceDesc = userService.sortBookByDesc();
		if(!sortBookByPriceDesc.isEmpty())
			return ResponseEntity.status(HttpStatus.OK)
					.body(new Response(environment.getProperty("user.bookdisplayed.hightolow"), HttpStatus.OK.value(), sortBookByPriceDesc));
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(new Response(HttpStatus.NOT_FOUND.value(), environment.getProperty("user.bookdisplayed.failed")));
	}
	@GetMapping("/getallBooks")
	public ResponseEntity<Response> getAllBooks()throws UserException
	{
		List<BookModel> book=userService.getAllBooks();
		return ResponseEntity.status(HttpStatus.ACCEPTED).body(new Response("Getting all the books which are verified", 200,book));
	}
	@GetMapping("/getbookdetails/{bookId}")
	public ResponseEntity<Response> getBookDetails(@PathVariable Long bookId)throws UserException
	{
		BookModel book=userService.getBookDetails(bookId);
		return ResponseEntity.status(HttpStatus.ACCEPTED).body(new Response("Getting book details", 200,book));
	}
	
	 @PostMapping("/uploadfile")
	    public ResponseEntity<Response> uploadFile(@RequestPart(value = "file") MultipartFile file,@RequestHeader String token ) throws UserException
	    {
	       UserModel user=this.amazonS3ClientService.uploadFileToS3Bucket(file, true,token);
			return ResponseEntity.status(HttpStatus.ACCEPTED).body(new Response("Image uploaded successfully", 200,user));

	    }

	    @DeleteMapping("/deletefile")
	    public Map<String, String> deleteFile(@RequestParam("file_name") String fileName,@RequestHeader String  token)
	    {
	        this.amazonS3ClientService.deleteFileFromS3Bucket(fileName);

	        Map<String, String> response = new HashMap<>();
	        response.put("message", "Removing request submitted successfully.");

	        return response;
	    }
	
	
	
}