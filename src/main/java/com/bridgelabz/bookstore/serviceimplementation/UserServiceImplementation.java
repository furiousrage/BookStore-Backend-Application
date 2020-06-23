package com.bridgelabz.bookstore.serviceimplementation;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.bridgelabz.bookstore.dto.RegistrationDto;
import com.bridgelabz.bookstore.exception.InvalidCredentialsException;
import com.bridgelabz.bookstore.exception.UserVerificationException;
import com.bridgelabz.bookstore.model.UserModel;
import com.bridgelabz.bookstore.repository.UserRepository;
import com.bridgelabz.bookstore.response.EmailObject;
import com.bridgelabz.bookstore.response.Response;
import com.bridgelabz.bookstore.service.UserService;
import com.bridgelabz.bookstore.utility.JwtGenerator;
import com.bridgelabz.bookstore.utility.RabbitMQSender;
import com.bridgelabz.bookstore.utility.RedisTempl;
import com.bridgelabz.bookstore.utility.TokenData;
import com.bridgelabz.bookstore.utility.Utils;

@Service
public class UserServiceImplementation implements UserService {

	 @Autowired
	 private BCryptPasswordEncoder bCryptPasswordEncoder;

	 @Autowired
	 private UserRepository repository;
	 
	 @Autowired
	 private RabbitMQSender rabbitMQSender;
	 
	 @Autowired
	 private RedisTempl<Object> redis;

	 private String redisKey = "Key";
	 
	@Override
	public ResponseEntity<Response> register(RegistrationDto registrationDto) {
		 UserModel emailavailable = repository.findEmail(registrationDto.getEmailId());
		 if (emailavailable != null) {
			 return ResponseEntity.status(HttpStatus.ALREADY_REPORTED).body(new Response(Utils.BAD_REQUEST_RESPONSE_CODE, "User already exist"));
		 }else {
			UserModel userDetails = new UserModel(registrationDto.getFullName(), registrationDto.getEmailId(), registrationDto.getMobileNumber(), registrationDto.getPassword());
			userDetails.setRegisteredAt(LocalDateTime.now());
			userDetails.setUpdatedAt(LocalDateTime.now());
			userDetails.setVerified(false);
			userDetails.setPassword(bCryptPasswordEncoder.encode(userDetails.getPassword()));
			repository.insertdata(registrationDto.getFullName(), registrationDto.getEmailId(), registrationDto.getMobileNumber(), bCryptPasswordEncoder.encode(registrationDto.getPassword()), false, LocalDateTime.now(), LocalDateTime.now() );
			UserModel sendMail = repository.findEmail(registrationDto.getEmailId());
			String response = TokenData.verifyResponse(sendMail.getUserId());
			redis.putMap(redisKey, userDetails.getEmailId(), userDetails.getFullName());
			if(rabbitMQSender.send(new EmailObject(sendMail.getEmailId(),"Registration Link...",response)))
				return ResponseEntity.status(HttpStatus.OK).body(new Response(Utils.OK_RESPONSE_CODE, "Registration Successfull"));
		}
		throw new InvalidCredentialsException( Utils.USER_AUTHENTICATION_EXCEPTION_STATUS,"Invalid Credentials"); 
	}

	@Override
	public ResponseEntity<Response> verify(String token) {
		long id = JwtGenerator.decodeJWT(token);
		UserModel userInfo = repository.findById(id);
		if (id > 0 && userInfo != null) {
			if (!userInfo.isVerified()) {
				userInfo.setVerified(true);
				userInfo.setUpdatedAt(LocalDateTime.now());
				repository.updatedAt(userInfo.getUserId()); 
				repository.verify(userInfo.getUserId());
				return ResponseEntity.status(HttpStatus.OK).body(new Response(Utils.OK_RESPONSE_CODE, "Verified Successfully"));
			} 
			throw new UserVerificationException(Utils.ALREADY_VERIFIED_EXCEPTION_STATUS, "User already verified!");
		}
		return ResponseEntity.status(HttpStatus.OK).body(new Response(Utils.BAD_REQUEST_RESPONSE_CODE, "Not Verified"));
	}

	@Override
	public ResponseEntity<Response> forgetPassword(RegistrationDto registrationDto) {
		UserModel isIdAvailable = repository.findEmail(registrationDto.getEmailId());
		if (isIdAvailable != null && isIdAvailable.isVerified() == true) {
			String response = TokenData.verifyResponse(isIdAvailable.getUserId());
			if(rabbitMQSender.send(new EmailObject(isIdAvailable.getEmailId(),"ResetPassord Link...",response)))
				return ResponseEntity.status(HttpStatus.OK).body(new Response(Utils.OK_RESPONSE_CODE, "Password is send to the Email-Id"));
		}
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Response(Utils.BAD_REQUEST_RESPONSE_CODE, "Sorry!! User Doesn't Exist"));
	}

}
