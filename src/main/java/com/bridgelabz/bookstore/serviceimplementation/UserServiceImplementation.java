package com.bridgelabz.bookstore.serviceimplementation;

import java.time.LocalDateTime;
import java.util.List;

import com.bridgelabz.bookstore.dto.*;
import com.bridgelabz.bookstore.exception.BookException;
import com.bridgelabz.bookstore.model.*;
import com.bridgelabz.bookstore.repository.*;
import com.bridgelabz.bookstore.response.UserAddressDetailsResponse;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.bridgelabz.bookstore.exception.UserException;
import com.bridgelabz.bookstore.exception.UserNotFoundException;
import com.bridgelabz.bookstore.exception.UserVerificationException;
import com.bridgelabz.bookstore.response.EmailObject;
import com.bridgelabz.bookstore.response.Response;
import com.bridgelabz.bookstore.response.UserDetailsResponse;
import com.bridgelabz.bookstore.service.UserService;
import com.bridgelabz.bookstore.utility.JwtGenerator;
import com.bridgelabz.bookstore.utility.RabbitMQSender;
import com.bridgelabz.bookstore.utility.RedisTempl;

import static java.util.stream.Collectors.toList;

@Service
@PropertySource(name = "user", value = {"classpath:response.properties"})
public class UserServiceImplementation implements UserService {
    @Autowired
    private SellerRepository sellerRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserDetailsRepository userDetailsRepository;

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
	
	@Autowired
	private JwtGenerator jwtop;

    private String redisKey = "Key";

    private static final long REGISTRATION_EXP = (long) 10800000;
    private static final String VERIFICATION_URL = "http://localhost:8080/user/verify/";
    private static final String RESETPASSWORD_URL = "http://localhost:8080/user/resetpassword?token=";

    @Override
    public boolean register(RegistrationDto registrationDto) throws UserException {
        UserModel emailavailable = userRepository.findByEmailId(registrationDto.getEmailId());
        if (emailavailable != null) {
            return false;
        } else {
            UserModel userDetails = new UserModel();
            BeanUtils.copyProperties(registrationDto, userDetails);
            userDetails.setPassword(bCryptPasswordEncoder.encode(userDetails.getPassword()));
            long id = userRepository.save(userDetails).getUserId();
            UserModel sendMail = userRepository.findByEmailId(registrationDto.getEmailId());
            String response = VERIFICATION_URL + JwtGenerator.createJWT(sendMail.getUserId(), REGISTRATION_EXP);
            redis.putMap(redisKey, userDetails.getEmailId(), userDetails.getFullName());
            switch (registrationDto.getRoleType()) {
                case SELLER:
                    SellerModel sellerDetails = new SellerModel();
                    sellerDetails.setSellerName(registrationDto.getFullName());
                    sellerDetails.setEmailId(registrationDto.getEmailId());
                    sellerDetails.setUserId(id);
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
        UserModel userInfo = userRepository.findByUserId(id);
        if (id > 0 && userInfo != null) {
            if (!userInfo.isVerified()) {
                userInfo.setVerified(true);
                userInfo.setUpdatedAt(LocalDateTime.now());
                userRepository.save(userInfo);
                return true;
            }
            throw new UserVerificationException(HttpStatus.CREATED.value(),
                    environment.getProperty("user.already.verified"));
        }
        return false;
    }

    @Override
    public UserDetailsResponse forgetPassword(ForgotPasswordDto userMail) {
        UserModel isIdAvailable = userRepository.findByEmailId(userMail.getEmailId());
        if (isIdAvailable != null && isIdAvailable.isVerified()) {
            String token = JwtGenerator.createJWT(isIdAvailable.getUserId(), REGISTRATION_EXP);
            String response = RESETPASSWORD_URL + token;
            if (rabbitMQSender.send(new EmailObject(isIdAvailable.getEmailId(), "ResetPassword Link...", response)))
                return new UserDetailsResponse(HttpStatus.OK.value(), "ResetPassword link Successfully", token);
        }
        return new UserDetailsResponse(HttpStatus.OK.value(), "Eamil ending failed");
    }

    @Override
    public boolean resetPassword(ResetPasswordDto resetPassword, String token) throws UserNotFoundException {
        if (resetPassword.getNewPassword().equals(resetPassword.getConfirmPassword())) {
            long id = JwtGenerator.decodeJWT(token);
            UserModel isIdAvailable = userRepository.findByUserId(id);
            if (isIdAvailable != null) {
                isIdAvailable.setPassword(bCryptPasswordEncoder.encode((resetPassword.getNewPassword())));
                userRepository.save(isIdAvailable);
                redis.putMap(redisKey, resetPassword.getNewPassword(), token);
                return true;
            }
            throw new UserNotFoundException(environment.getProperty("user.not.exist"));
        }
        return false;
    }

    @Override
    public Response login(LoginDto loginDTO) throws UserNotFoundException, UserException {
        UserModel userCheck = userRepository.findByEmailId(loginDTO.getEmailId());

        if (userCheck == null) {
            throw new UserNotFoundException("user.not.exist");
        }
        if (bCryptPasswordEncoder.matches(loginDTO.getPassword(), userCheck.getPassword())) {

            String token = JwtGenerator.createJWT(userCheck.getUserId(), REGISTRATION_EXP);

            redis.putMap(redisKey, userCheck.getEmailId(), userCheck.getPassword());
            userCheck.setUserStatus(true);
            userRepository.save(userCheck);
            return new Response(HttpStatus.OK.value(), token);
        }

        throw new UserException(environment.getProperty("user.invalid.credential"));

    }

    @Override
    public Response addToCart(Long bookId) throws BookException {
        BookModel bookModel = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookException(environment.getProperty("book.not.exist"),HttpStatus.NOT_FOUND));

        if (bookModel.isVerfied()) {
            CartModel cartModel = new CartModel();
            cartModel.setBook_id(bookId);
            cartModel.setName(bookModel.getBookName());
            cartModel.setAuthor(bookModel.getAuthorName());
            cartModel.setTotalPrice(bookModel.getPrice());
            cartModel.setQuantity(1);
            cartRepository.save(cartModel);
            return new Response(environment.getProperty("book.added.to.cart.successfully"), HttpStatus.OK.value(), cartModel);
        }
        throw new BookException(environment.getProperty("book.unverified"), HttpStatus.OK);

    }

    @Override
    public Response addMoreItems(Long bookId) throws BookException {

        CartModel cartModel = cartRepository.findByBookId(bookId).get();

        BookModel bookModel= bookRepository.findByBookId(bookId);
        if(cartModel.getQuantity()>0){
            cartModel.setQuantity(cartModel.getQuantity()+1);
            cartModel.setTotalPrice(bookModel.getPrice()*cartModel.getQuantity());
            cartRepository.save(cartModel);
        }
        return new Response( HttpStatus.OK.value(),environment.getProperty("book.added.to.cart.successfully"));
    }

    @Override
    public Response removeItem(Long bookId) throws BookException {

        CartModel cartModel = cartRepository.findByBookId(bookId).get();
             BookModel bookModel= bookRepository.findByBookId(bookId);
        if(cartModel.getQuantity()>0){
            cartModel.setQuantity(cartModel.getQuantity()-1);
            cartModel.setTotalPrice(bookModel.getPrice()*cartModel.getQuantity());
            cartRepository.save(cartModel);

        }
        return new Response( HttpStatus.OK.value(),environment.getProperty("one.quantity.removed.success"));
    }

    @Override
    public Response removeAllItem(Long bookId) {
        cartRepository.delete(cartRepository.findByBookId(bookId).get());
        return new Response(HttpStatus.OK.value(), environment.getProperty("quantity.removed.success"));
    }

    @Override
    public List<CartModel> getAllItemFromCart() throws BookException {
        List<CartModel> items = cartRepository.findAll();
        if (items.isEmpty())
            throw new BookException(environment.getProperty("cart.empty"), HttpStatus.NOT_FOUND);
        return items;
    }

    @Override
    public List<BookModel> sortBookByAsc() {
        return bookRepository.sortBookAsc();
    }

    @Override
    public List<BookModel> sortBookByDesc() {
        return bookRepository.sortBookDesc();
    }


    @Override
    public List<BookModel> getAllBooks() throws UserException
    {
        List<BookModel> booklist=bookRepository.getAllBooks();
        return booklist;
    }

    @Override
    public BookModel getBookDetails(Long bookid) throws UserException
    {
        BookModel bookdetail=bookRepository.getBookDetail(bookid);
        return bookdetail;
    }


//	@Override
//	public BookModel getBookDetails(Long bookid) throws UserException
//	{
//	  BookModel bookdetail=bookRepository.getBookDetail(bookid);
//	  if(bookdetail==null)
//	  {
//		  throw new UserException("Book is not available",null);
//	  }
//		return bookdetail;
//	}
//
//    @Override
//    public Response addToCart(String token, Long bookId) throws BookException, UserNotFoundException {
//        BookModel bookModel = bookRepository.findById(bookId)
//                .orElseThrow(() -> new UserNotFoundException(environment.getProperty("book.not.exist")));
//
//        Long userId = JwtGenerator.decodeJWT(token);
//        if (bookModel.isVerfied()) {
//            CartModel cartModel = new CartModel();
//            cartModel.setBook_id(bookId);
//            cartModel.setQuantity(1L);
//            cartModel.setId(userId);
//            cartRepository.save(cartModel);
//        }
//        throw new BookException("Book is not verified by Admin ", HttpStatus.OK);
//
//    }

    /************************ user details ****************************/
    @Override
    public UserAddressDetailsResponse getUserDetails(long userId) {
        UserModel user = userRepository.findByUserId(userId);
        List<UserDetailsDTO> allDetailsByUser = user.getListOfUserDetails().stream().map(this::mapData).collect(toList());
        if (allDetailsByUser.isEmpty())
            return new UserAddressDetailsResponse(HttpStatus.OK.value(), environment.getProperty("user.details.nonAvailable"));
        return new UserAddressDetailsResponse(HttpStatus.OK.value(), environment.getProperty("user.details.available"), allDetailsByUser);
    }

    private UserDetailsDTO mapData(UserDetailsDAO details) {
        UserDetailsDTO userDto = new UserDetailsDTO();
        BeanUtils.copyProperties(details, userDto);
        return userDto;
    }

    @Override
    public Response addUserDetails(UserDetailsDTO userDetail, long userId) {
        //long userId=JwtGenerator.decodeJWT(token);
        UserDetailsDAO userDetailsDAO = new UserDetailsDAO();
        BeanUtils.copyProperties(userDetail, userDetailsDAO);
        UserModel user = userRepository.findByUserId(userId);
        userDetailsDAO.setUserId(userId);
        user.addUserDetails(userDetailsDAO);
        userRepository.save(user);
        userDetailsDAO.setUser(user);
        userDetailsRepository.save(userDetailsDAO);
        return new Response(HttpStatus.OK.value(), environment.getProperty("user.details.added"));
    }

    @Override
    public Response deleteUserDetails(UserDetailsDTO userDetail, long userId) {
        UserModel userModel = userRepository.findByUserId(userId);
        UserDetailsDAO userDetailsDAO = userDetailsRepository.findByAddressAndUserId(userDetail.getAddress(), userId);
        userModel.removeUserDetails(userDetailsDAO);
        userDetailsRepository.delete(userDetailsDAO);
        userRepository.save(userModel);
        return new Response(HttpStatus.OK.value(), environment.getProperty("user.details.deleted"));
    }

    @Override
     public long getOrderId(){
        return (long) (Math.random() * 45678) + 999999;
     }




}
