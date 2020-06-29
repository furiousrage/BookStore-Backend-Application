package com.bridgelabz.bookstore.serviceimplementation;

import java.time.LocalDateTime;
import java.util.List;

import com.bridgelabz.bookstore.exception.BookException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.bridgelabz.bookstore.dto.ForgotPasswordDto;
import com.bridgelabz.bookstore.dto.LoginDto;
import com.bridgelabz.bookstore.dto.RegistrationDto;
import com.bridgelabz.bookstore.dto.ResetPasswordDto;
import com.bridgelabz.bookstore.exception.UserException;
import com.bridgelabz.bookstore.exception.UserNotFoundException;
import com.bridgelabz.bookstore.exception.UserVerificationException;
import com.bridgelabz.bookstore.model.AdminModel;
import com.bridgelabz.bookstore.model.BookModel;
import com.bridgelabz.bookstore.model.CartModel;
import com.bridgelabz.bookstore.model.SellerModel;
import com.bridgelabz.bookstore.model.UserModel;
import com.bridgelabz.bookstore.repository.AdminRepository;
import com.bridgelabz.bookstore.repository.BookRepository;
import com.bridgelabz.bookstore.repository.CartRepository;
import com.bridgelabz.bookstore.repository.SellerRepository;
import com.bridgelabz.bookstore.repository.UserRepository;
import com.bridgelabz.bookstore.response.EmailObject;
import com.bridgelabz.bookstore.response.Response;
import com.bridgelabz.bookstore.service.UserService;
import com.bridgelabz.bookstore.utility.JwtGenerator;
import com.bridgelabz.bookstore.utility.RabbitMQSender;
import com.bridgelabz.bookstore.utility.RedisTempl;

@Service
@PropertySource(name = "user", value = { "classpath:response.properties" })
public class UserServiceImplementation implements UserService {

	@Autowired
	private SellerRepository sellerRepository;

	@Autowired
	private AdminRepository adminRepository;

	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	@Autowired
	private UserRepository repository;

	@Autowired
	private BookRepository bookRepository;

	@Autowired
	private Environment environment;

	@Autowired
	private CartRepository cartRepository;

	@Autowired
	private RabbitMQSender rabbitMQSender;

	@Autowired
	private RedisTempl<Object> redis;

	private String redisKey = "Key";

	private static final long REGISTRATION_EXP = (long) 10800000;
	private static final String VERIFICATION_URL = "http://localhost:8080/user/verify/";
	private static final String RESETPASSWORD_URL = "http://localhost:8080/user/resetpassword/";

	@Override
	public boolean register(RegistrationDto registrationDto) throws UserException {
		UserModel emailavailable = repository.findEmail(registrationDto.getEmailId());
		if (emailavailable != null) {
			return false;
		} else {
			UserModel userDetails = new UserModel();
			BeanUtils.copyProperties(registrationDto, userDetails);
			userDetails.setPassword(bCryptPasswordEncoder.encode(userDetails.getPassword()));
			repository.save(userDetails);
			UserModel sendMail = repository.findEmail(registrationDto.getEmailId());
			String response = VERIFICATION_URL + JwtGenerator.createJWT(sendMail.getUserId(), REGISTRATION_EXP);
			redis.putMap(redisKey, userDetails.getEmailId(), userDetails.getFullName());
			switch (registrationDto.getRoleType()) {
			case SELLER:
				SellerModel sellerDetails = new SellerModel();
				sellerDetails.setSellerName(registrationDto.getFullName());
				sellerDetails.setEmailId(registrationDto.getEmailId());
				sellerRepository.save(sellerDetails);
				break;
			case ADMIN:
				AdminModel adminDetails = new AdminModel();
				adminDetails.setAdminName(registrationDto.getFullName());
				adminDetails.setEmailId(registrationDto.getEmailId());
				adminRepository.save(adminDetails);
				break;
			}
			if (rabbitMQSender.send(new EmailObject(sendMail.getEmailId(), "Registration Link...", response)))
				return true;

		}
		throw new UserException(environment.getProperty("user.invalidcredentials"), HttpStatus.FORBIDDEN);
	}

	@Override
	public boolean verify(String token) {
		long id = JwtGenerator.decodeJWT(token);
		UserModel userInfo = repository.findById(id);
		if (id > 0 && userInfo != null) {
			if (!userInfo.isVerified()) {
				userInfo.setVerified(true);
				userInfo.setUpdatedAt(LocalDateTime.now());
				repository.updatedAt(userInfo.getUserId());
				repository.verify(userInfo.getUserId());
				return true;
			}
			throw new UserVerificationException(HttpStatus.CREATED.value(),
					environment.getProperty("user.already.verified"));
		}
		return false;
	}

	@Override
	public boolean forgetPassword(ForgotPasswordDto userMail) {
		UserModel isIdAvailable = repository.findEmail(userMail.getEmailId());
		if (isIdAvailable != null && isIdAvailable.isVerified() == true) {
			String response = RESETPASSWORD_URL + JwtGenerator.createJWT(isIdAvailable.getUserId(), REGISTRATION_EXP);
			if (rabbitMQSender.send(new EmailObject(isIdAvailable.getEmailId(), "ResetPassword Link...", response)))
				return true;
		}
		return false;
	}

	@Override
	public boolean resetPassword(ResetPasswordDto resetPassword, String token) throws UserNotFoundException {
		if (resetPassword.getNewPassword().equals(resetPassword.getConfirmPassword())) {
			long id = JwtGenerator.decodeJWT(token);
			UserModel isIdAvailable = repository.findById(id);
			if (isIdAvailable != null) {
				isIdAvailable.setPassword(bCryptPasswordEncoder.encode((resetPassword.getNewPassword())));
				repository.save(isIdAvailable);
				redis.putMap(redisKey, resetPassword.getNewPassword(), token);
				return true;
			}
			throw new UserNotFoundException(environment.getProperty("user.not.exist"));
		}
		return false;
	}

	@Override
	public Response login(LoginDto loginDTO) throws UserNotFoundException, UserException {
		UserModel userCheck = repository.findEmail(loginDTO.getEmail());

		if (userCheck == null) {
			throw new UserNotFoundException("user.not.exist");
		}
		if (bCryptPasswordEncoder.matches(loginDTO.getPassword(), userCheck.getPassword())) {

			String token = JwtGenerator.createJWT(userCheck.getUserId(), REGISTRATION_EXP);

			redis.putMap(redisKey, userCheck.getEmailId(), userCheck.getPassword());
			userCheck.setUserStatus(true);
			repository.save(userCheck);
			return new Response(HttpStatus.OK.value(), token);
		}

		throw new UserException(environment.getProperty("user.invalidcredential"));

	}

	@Override
	public Response addToCart(String token, Long bookId) throws BookException, UserNotFoundException {
		BookModel bookModel = bookRepository.findById(bookId)
				.orElseThrow(() -> new UserNotFoundException(environment.getProperty("book.not.exist")));

		Long userId = JwtGenerator.decodeJWT(token);
		if (bookModel.isVerfied()) {
			CartModel cartModel = new CartModel();
			cartModel.setBook_id(bookId);
			cartModel.setQuantity(1L);
			cartModel.setUser_id(userId);
			cartRepository.save(cartModel);
		}
		throw new BookException("Book is not verified by Admin ", HttpStatus.OK);

	}

	@Override
	public Response addMoreItems(Long bookId) throws BookException {

		CartModel cartModel = cartRepository.findByBookId(bookId)
				.orElseThrow(() -> new BookException(environment.getProperty("book.not.added"), HttpStatus.NOT_FOUND));

		long quantity = cartModel.getQuantity();
		cartModel.setTotalPrice(cartModel.getTotalPrice() * (quantity + 1) / quantity);
		quantity++;
		cartModel.setQuantity(quantity);
		cartRepository.save(cartModel);
		return new Response("Book Added to Cart Successfully", HttpStatus.OK.value(), cartModel);
	}

	@Override
	public Response removeItem(Long bookId) throws BookException {

		CartModel cartModel = cartRepository.findByBookId(bookId)
				.orElseThrow(() -> new BookException("Book Not Added to Cart", HttpStatus.NOT_FOUND));
		long quantity = cartModel.getQuantity();
		if (quantity == 1) {
			cartRepository.deleteById(cartModel.getId());
			return new Response(HttpStatus.OK.value(), environment.getProperty("items.removed.success"));
		}
		cartModel.setTotalPrice(cartModel.getTotalPrice() * (quantity - 1) / quantity);
		quantity--;
		cartModel.setQuantity(quantity);
		cartRepository.save(cartModel);
		return new Response("One Quantity Removed Successfully", HttpStatus.OK.value(), cartModel);
	}

	@Override
	public Response removeAllItem() {
		cartRepository.deleteAll();
		return new Response(HttpStatus.OK.value(), "Items Removed Successfully");
	}

	@Override
	public List<CartModel> getAllItemFromCart() throws BookException {
		List<CartModel> items = cartRepository.findAll();
		if (items.isEmpty())
			throw new BookException("Cart is Empty", HttpStatus.NOT_FOUND);
		return items;
	}

}
