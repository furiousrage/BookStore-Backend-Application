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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.bridgelabz.bookstore.dto.ForgotPasswordDto;
import com.bridgelabz.bookstore.dto.LoginDto;
import com.bridgelabz.bookstore.dto.RegistrationDto;
import com.bridgelabz.bookstore.dto.ResetPasswordDto;
import com.bridgelabz.bookstore.dto.UserDetailsDTO;
import com.bridgelabz.bookstore.exception.BookException;
import com.bridgelabz.bookstore.exception.UserException;
import com.bridgelabz.bookstore.exception.UserNotFoundException;
import com.bridgelabz.bookstore.model.BookModel;
import com.bridgelabz.bookstore.model.CartModel;
import com.bridgelabz.bookstore.response.Response;
import com.bridgelabz.bookstore.response.UserAddressDetailsResponse;
import com.bridgelabz.bookstore.response.UserDetailsResponse;
import com.bridgelabz.bookstore.service.ElasticSearchService;
import com.bridgelabz.bookstore.service.UserService;
import com.bridgelabz.bookstore.serviceimplementation.AmazonS3ClientServiceImpl;
import com.bridgelabz.bookstore.utility.JwtGenerator;

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
     private AmazonS3ClientServiceImpl amazonS3ClientService;

     
	
	@PostMapping("/register")
	public ResponseEntity<Response> register(@RequestBody @Valid  RegistrationDto registrationDto,BindingResult result)throws UserException {

		if (result.hasErrors())
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(result.getAllErrors().get(0).getDefaultMessage(), HttpStatus.BAD_REQUEST.value(), "Invalid Credentials"));

		if (userService.register(registrationDto))
			return ResponseEntity.status(HttpStatus.OK)
					.body(new Response(HttpStatus.OK.value(), environment.getProperty("user.register.successful")));
		return ResponseEntity.status(HttpStatus.OK)
				.body(new Response(HttpStatus.BAD_REQUEST.value(), environment.getProperty("user.register.unsuccessful")));
	}

	@GetMapping("/verify/{token}")
	public ResponseEntity<Response> userVerification(@PathVariable("token") String token) {

		if (userService.verify(token))
			return ResponseEntity.status(HttpStatus.OK)
					.body(new Response(HttpStatus.OK.value(), environment.getProperty("user.verified.successful")));

		return ResponseEntity.status(HttpStatus.OK).body(new Response(HttpStatus.BAD_REQUEST.value(), environment.getProperty("user.verified.unsuccessfull")));
	}

	@PostMapping("/forgotpassword")
	public ResponseEntity<UserDetailsResponse> forgotPassword(@RequestBody @Valid ForgotPasswordDto emailId) {

		UserDetailsResponse response= userService.forgetPassword(emailId);
		return new ResponseEntity<UserDetailsResponse>(response, HttpStatus.OK);
	}
	
	@PutMapping("/resetpassword")
	public ResponseEntity<Response> resetPassword(@RequestBody @Valid ResetPasswordDto resetPassword,
			@RequestParam("token") String token) throws UserNotFoundException {

		if (userService.resetPassword(resetPassword, token))
			return ResponseEntity.status(HttpStatus.OK)
					.body(new Response(HttpStatus.OK.value(), environment.getProperty("user.resetpassword.successfull")));

		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(new Response(HttpStatus.BAD_REQUEST.value(), environment.getProperty("user.resetpassword.failed")));
	}

	@ApiOperation(value = "To login")
	@PostMapping("/login")
	public ResponseEntity<Response> login(@RequestBody LoginDto loginDTO) throws UserNotFoundException, UserException {
		Response response = userService.login(loginDTO);
		return new ResponseEntity<Response>(response, HttpStatus.OK);
	}

	@ApiOperation(value = "Add Books to Cart")
	@PostMapping("/AddToCart")
	public ResponseEntity<Response> AddToCart(@RequestParam Long bookId) throws BookException {
		Response response = userService.addToCart(bookId);
		return new ResponseEntity<Response>(response, HttpStatus.OK);
	}

	@ApiOperation(value = "Adding More Items To Cart")
	@PostMapping("/addMoreItems")
	public ResponseEntity<Response> addMoreItems(@RequestParam Long bookId) throws BookException {
		Response response = userService.addMoreItems(bookId);
		return new ResponseEntity
				<Response>(response, HttpStatus.OK);
	}

	@ApiOperation(value = "Remove Items from Cart")
	@DeleteMapping("/removeFromCart")
	public ResponseEntity<Response> removeFromCart(@RequestParam Long bookId) throws BookException {
		Response response = userService.removeItem(bookId);
		return new ResponseEntity<Response>(response, HttpStatus.OK);
	}

	@ApiOperation(value = "Remove All Items from Cart")
	@DeleteMapping("/removeAllFromCart/{bookId}")
	public ResponseEntity<Response> removeAllFromCart(@PathVariable Long bookId) {
		Response response = userService.removeAllItem(bookId);
		return new ResponseEntity<Response>(response, HttpStatus.OK);
	}

	@ApiOperation(value = "Get All Items from Cart")
	@GetMapping("/getAllFromCart")
	public List<CartModel> getAllItemsFromCart() throws BookException {
		return userService.getAllItemFromCart();
	}

	@ApiOperation(value = "Add Book to Elastic Search")
	@PostMapping("/search")
	public List<BookModel> search(@RequestParam String searchItem) {
		return elasticSearchService.searchByTitle(searchItem);
	}
	
	@GetMapping("/getBooksByPriceAsc")
	public ResponseEntity<Response> sortBookByPriceAsc(){
		List<BookModel> sortBookByPriceAsc = userService.sortBookByAsc();
		if(!sortBookByPriceAsc.isEmpty()) 
			return ResponseEntity.status(HttpStatus.OK)
					.body(new Response( environment.getProperty("user.bookDisplayed.lowToHigh"), HttpStatus.OK.value(), sortBookByPriceAsc));
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(new Response(HttpStatus.NOT_FOUND.value(), environment.getProperty("user.bookDisplayed.failed")));
	}
	
	@GetMapping("/getBooksByPriceDesc")
	public ResponseEntity<Response> sortBookByPriceDesc(){
		List<BookModel> sortBookByPriceDesc = userService.sortBookByDesc();
		if(!sortBookByPriceDesc.isEmpty())
			return ResponseEntity.status(HttpStatus.OK)
					.body(new Response(environment.getProperty("user.bookDisplayed.highToLow"), HttpStatus.OK.value(), sortBookByPriceDesc));
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(new Response(HttpStatus.NOT_FOUND.value(), environment.getProperty("user.bookDisplayed.failed")));
	}

	@GetMapping("/getUserDetails")
	public ResponseEntity<UserAddressDetailsResponse> getUserDetails(@RequestParam long id){
		return  ResponseEntity.status(HttpStatus.OK).body(userService.getUserDetails(id));
	}

	@PostMapping("/addUserDetails")
	public ResponseEntity<Response> addUserDetails(@RequestBody UserDetailsDTO userDetailsDTO,@RequestParam String token){
		return ResponseEntity.status(HttpStatus.OK).body(userService.addUserDetails(userDetailsDTO, JwtGenerator.decodeJWT(token)));
	}

	@DeleteMapping("/deleteUserDetails")
	public ResponseEntity<Response> deleteUserDetails(@RequestBody UserDetailsDTO userDetailsDTO, @RequestParam long userId){
		return ResponseEntity.status(HttpStatus.OK).body(userService.deleteUserDetails(userDetailsDTO,userId));
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

	@PostMapping("/uploadFile")
	public ResponseEntity<Response> uploadFile(@RequestParam("file") MultipartFile file) {
		String url = amazonS3ClientService.uploadFile(file);
		return ResponseEntity.status(HttpStatus.ACCEPTED).body(new Response("Uploaded successfully", 200,url));
	}


	@DeleteMapping("/deleteFile")
	public String deleteFile(@RequestPart(value = "url") String fileUrl) {
		return amazonS3ClientService.deleteFileFromS3Bucket(fileUrl);
	}


	/* OrderIDGeneratorMethod */
	/*@GetMapping("/orderId/{token}")
	public long getOrderId(@PathVariable String token){
		return userService.getOrderId(token);
	}*/
}