package com.bridgelabz.bookstore.serviceimplementation;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.bridgelabz.bookstore.dto.BookDto;
import com.bridgelabz.bookstore.dto.UpdateBookDto;
import com.bridgelabz.bookstore.exception.UserException;
import com.bridgelabz.bookstore.model.BookModel;
import com.bridgelabz.bookstore.model.UserModel;
import com.bridgelabz.bookstore.repository.BookRepository;
import com.bridgelabz.bookstore.repository.SellerRepository;
import com.bridgelabz.bookstore.repository.UserRepository;
import com.bridgelabz.bookstore.response.Response;
import com.bridgelabz.bookstore.service.ElasticSearchService;
import com.bridgelabz.bookstore.service.SellerService;
import com.bridgelabz.bookstore.utility.JwtGenerator;

@Service
public class SellerServiceImplementation implements SellerService {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private BookRepository bookRepository;

	@Autowired
	private ElasticSearchService elasticSearchService;

	@Autowired
	private AmazonS3ClientServiceImpl amazonS3Client;

	@Autowired
	private Environment environment;

	@Override
	public Response addBook(BookDto newBook,MultipartFile multipartFile, String token) throws UserException {
		Long id = JwtGenerator.decodeJWT(token);
		String role = userRepository.checkRole(id);
		Optional<UserModel> user = userRepository.findById(id);
		if (role.equals("SELLER")) {
			BookModel book = new BookModel();
			BeanUtils.copyProperties(newBook, book);
			String imgUrl = amazonS3Client.uploadFile(multipartFile);
			book.setBookImgUrl(imgUrl);
			bookRepository.save(book);
			//elasticSearchService.addBook(book);
			return new Response(environment.getProperty("book.verification.status"), HttpStatus.OK.value(), book);

		} else {
			throw new UserException(environment.getProperty("book.unauthorised.status"));
		}

	}

	@Override
	public Response updateBook(UpdateBookDto newBook, String token, Long bookId) throws UserException {
		long id = JwtGenerator.decodeJWT(token);
		String role = userRepository.checkRole(id);
		if (role.equals("SELLER")) {
			Optional<BookModel> book = bookRepository.findById(bookId);
			BeanUtils.copyProperties(newBook, book.get());
			book.get().setUpdatedDateAndTime(LocalDateTime.now());
			bookRepository.save(book.get());
			elasticSearchService.updateBook(book.get());
			return new Response(HttpStatus.OK.value(), "Book update Successfully Need to Verify");

		}
		return new Response(HttpStatus.OK.value(), "Book Not updated Becoz Not Authoriized to add Book");
	}

	@Override
	public Response deleteBook(String token, Long bookId) {
		long id = JwtGenerator.decodeJWT(token);
		String role = userRepository.checkRole(id);
		if (role.equals("SELLER")) {
			bookRepository.deleteById(bookId);
			elasticSearchService.deleteNote(bookId);
			return new Response(HttpStatus.OK.value(), "Book deleted Successfully ");

		}
		return new Response(HttpStatus.OK.value(), "Book Not deleted Becoz Not Authoriized to delete Book");
	}

}
