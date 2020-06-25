package com.bridgelabz.bookstore.serviceimplementation;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.bridgelabz.bookstore.dto.ForgotPasswordDto;
import com.bridgelabz.bookstore.dto.LoginDto;
import com.bridgelabz.bookstore.dto.RegistrationDto;
import com.bridgelabz.bookstore.dto.ResetPasswordDto;
import com.bridgelabz.bookstore.exception.UserException;
import com.bridgelabz.bookstore.exception.UserNotFoundException;
import com.bridgelabz.bookstore.exception.UserVerificationException;
import com.bridgelabz.bookstore.model.UserModel;
import com.bridgelabz.bookstore.repository.UserRepository;
import com.bridgelabz.bookstore.response.EmailObject;
import com.bridgelabz.bookstore.response.Response;
import com.bridgelabz.bookstore.response.UserDetailsResponse;
import com.bridgelabz.bookstore.service.UserService;
import com.bridgelabz.bookstore.utility.JwtGenerator;
import com.bridgelabz.bookstore.utility.RabbitMQSender;
import com.bridgelabz.bookstore.utility.RedisTempl;
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
	public boolean register(RegistrationDto registrationDto) throws UserException {
		 UserModel emailavailable = repository.findEmail(registrationDto.getEmailId());
		 if (emailavailable != null) {
			 return false;
		 }else {
			UserModel userDetails = new UserModel(registrationDto.getFullName(), registrationDto.getEmailId(), registrationDto.getMobileNumber(), registrationDto.getPassword());
			userDetails.setPassword(bCryptPasswordEncoder.encode(userDetails.getPassword()));
			repository.insertdata(registrationDto.getFullName(), registrationDto.getEmailId(), registrationDto.getMobileNumber(), bCryptPasswordEncoder.encode(registrationDto.getPassword()), false, LocalDateTime.now(), LocalDateTime.now() );
			UserModel sendMail = repository.findEmail(registrationDto.getEmailId());
			String response = Utils.VERIFICATION_URL + JwtGenerator.createJWT(sendMail.getUserId(), Utils.REGISTRATION_EXP);
			redis.putMap(redisKey, userDetails.getEmailId(), userDetails.getFullName());
			if(rabbitMQSender.send(new EmailObject(sendMail.getEmailId(),"Registration Link...",response)))
				return true;
		}
		throw new UserException("Invalid Credentials",HttpStatus.FORBIDDEN); 
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
			throw new UserVerificationException(Utils.ALREADY_VERIFIED_EXCEPTION_STATUS, "User already verified!");
		}
		return false;
	}

	@Override
	public boolean forgetPassword(ForgotPasswordDto userMail) {
		UserModel isIdAvailable = repository.findEmail(userMail.getEmailId());
		if (isIdAvailable != null && isIdAvailable.isVerified() == true) {
			String response = Utils.RESETPASSWORD_URL + JwtGenerator.createJWT(isIdAvailable.getUserId(), Utils.REGISTRATION_EXP);
			if(rabbitMQSender.send(new EmailObject(isIdAvailable.getEmailId(),"ResetPassord Link...",response)))
				return true;
		}
		return false;
	}

	@Override
	public boolean resetPassword(ResetPasswordDto resetPassword, String token) throws UserNotFoundException {
		if (resetPassword.getNewPassword().equals(resetPassword.getConfirmPassword()))	{
			long id = JwtGenerator.decodeJWT(token);
			UserModel isIdAvailable = repository.findById(id);
			if (isIdAvailable != null) {
				isIdAvailable.setPassword(bCryptPasswordEncoder.encode((resetPassword.getNewPassword())));
				repository.save(isIdAvailable);
				redis.putMap(redisKey, resetPassword.getNewPassword(),token);
				return true;
			}
			throw new UserNotFoundException("No User found");	
		}
		return false;
	}
	
	@Override
	public boolean login(LoginDto logindto) throws UserNotFoundException {
		 
		UserModel user = repository.findEmail(logindto.getEmail());
		if(user != null) {
			if (bCryptPasswordEncoder.matches(logindto.getPassword(),user.getPassword())) {
				if (user.isVerified()) {
					user.setUserStatus(true);
					redis.putMap(redisKey, user.getEmailId(), user.getPassword());
					repository.save(user);
					String token = JwtGenerator.createJWT(user.getUserId(),Utils.REGISTRATION_EXP);
					return true;
				}
			}
			return false;
		}
		throw new UserNotFoundException("User not found");
	 }

}
