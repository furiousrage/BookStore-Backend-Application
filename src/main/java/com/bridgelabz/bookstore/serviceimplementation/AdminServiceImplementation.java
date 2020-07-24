package com.bridgelabz.bookstore.serviceimplementation;

import java.util.List;
import java.util.Optional;

import com.bridgelabz.bookstore.model.SellerModel;
import com.bridgelabz.bookstore.model.UserModel;
import com.bridgelabz.bookstore.repository.SellerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.bridgelabz.bookstore.exception.UserNotFoundException;
import com.bridgelabz.bookstore.model.BookModel;
import com.bridgelabz.bookstore.repository.BookRepository;
import com.bridgelabz.bookstore.repository.UserRepository;
import com.bridgelabz.bookstore.response.EmailObject;
import com.bridgelabz.bookstore.response.Response;
import com.bridgelabz.bookstore.service.AdminService;
import com.bridgelabz.bookstore.utility.JwtGenerator;
import com.bridgelabz.bookstore.utility.MailServiceUtility;
import com.bridgelabz.bookstore.utility.RabbitMQSender;

@Service
public class AdminServiceImplementation implements AdminService {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private BookRepository bookRepository;
	
	@Autowired
	private SellerRepository sellerRepository;
	
	 @Autowired
	 private RabbitMQSender rabbitMQSender;
	 
	 @Autowired
	 private MailServiceUtility mailService;

	@Autowired
	private Environment environment;

	@Override
	public List<BookModel>  getAllUnVerifiedBooks(String token,Long sellerId) throws UserNotFoundException {

		long id = JwtGenerator.decodeJWT(token);
		String role = userRepository.checkRole(id);
		if(role.equals("ADMIN")){
			return bookRepository.getAllUnverfiedBooks(sellerId);
		}
		else {
			throw new UserNotFoundException("User is Not Authorized");
		}
	}

	@Override
	public Response bookVerification(Long bookId, String token) throws UserNotFoundException {
		long id = JwtGenerator.decodeJWT(token);
		String role = userRepository.checkRole(id);
		if(role.equals("ADMIN")){
			Optional<BookModel> book= bookRepository.findById(bookId);
			book.get().setVerfied(true);
			bookRepository.save(book.get());
			return new Response(environment.getProperty("book.verified.successfull"),HttpStatus.OK.value(),book);
		}
		else {
			throw new UserNotFoundException("User is Not Authorized");
		}
	}
	
//	@Override
//	public Response bookUnVerification(Long bookId, String token) throws UserNotFoundException {
//		long id = JwtGenerator.decodeJWT(token);
//		String role = userRepository.checkRole(id);
//		if(role.equals("ADMIN")){
//			Optional<BookModel> book= bookRepository.findById(bookId);
//			book.get().setVerfied(false);
//			book.get().setIsDisApproved(true);
//			bookRepository.save(book.get());
//			return new Response("Book Unverified SuccessFully",HttpStatus.OK.value(),book);
//		}
//		else {
//			throw new UserNotFoundException("User is Not Authorized");
//		}
//	}
	
	@Override
	public Response bookUnVerification(Long bookId, String token) throws UserNotFoundException {
		long id = JwtGenerator.decodeJWT(token);
		UserModel admin = userRepository.findByUserId(id);
		String role = userRepository.checkRole(id);
		if(role.equals("ADMIN")){
			Optional<BookModel> book= bookRepository.findById(bookId);
			System.out.println(book.get().getSellerId());
			Optional<UserModel> seller = userRepository.findById(book.get().getSellerId());
			System.out.println(seller);
			book.get().setVerfied(false);
			book.get().setIsDisApproved(true);
			int count = book.get().getRejectionCount() + 1;
			book.get().setRejectionCount(count);
			if (count > 1) {
				System.out.println(book);
				System.out.println("in the rejection");
				String message =
	                    "ONLINE BOOK STORE \n" +
	                    "=================\n\n" +
	                    "Hello " + seller.get().getFullName() + ",\n\n" +
	                    "Sorry to Inform that your request for Book Approval got Revoked.\n" +
	                    "-----------------------------------------------------------------" +
	                    "-----------------------------------------------------------------\n" +
	                    "Book Details : \n" +
	                    "--------------\n" +
	                    "Book Name : " + book.get().getBookName() + "\n" +
	                    "Author Name: " + book.get().getAuthorName() + "\n" +
	                    "Book Price : " + book.get().getPrice() + "\n" +
	                    "----------------------------------------------------------------- \n" +
	                    "Description of Rejection : \n" +
	                    "-------------------------- \n" +
	                    "Your Request for approval has been rejected because it doesn't fulfilled\n" +
	                    "Terms & Conditions of company policies.\n" +
	                    "------------------------------------------------------------------------" +
	                    "\n\n" +
	                    "Thank you,\n" +
	                    "Online Book Store Team, Bangalore\n";
				
				System.out.println(seller.get().getEmailId());
				if(rabbitMQSender.send(new EmailObject(seller.get().getEmailId(), "Book has been revoked", message))){
				bookRepository.delete(book.get());
				System.out.println(seller.get().getEmailId());
				return new Response("Book Unverified SuccessFully",HttpStatus.OK.value(),book);
				}
			}
			bookRepository.save(book.get());
			return new Response("Book Unverified SuccessFully",HttpStatus.OK.value(),book);
		}
		else {
			throw new UserNotFoundException("User is Not Authorized");
		}
	}
}	