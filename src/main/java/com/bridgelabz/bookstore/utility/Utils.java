package com.bridgelabz.bookstore.utility;

public class Utils {

	public static final String SECRET_KEY = "SCH567";
	public static final String ISSUER = "Bridgelabz";
	public static final String SUBJECT = "Authentication";
	public static final String VERIFICATION_URL = "http://localhost:8081/user/verify/";
	public static final long REGISTRATION_EXP = (long) 3 * 60 * 60 * 1000;
	public static final String SENDER_EMAIL_ID = "mdafsaransari720@gmail.com";
	public static final String SENDER_PASSWORD = "afsaralam1515";
	public static final int BAD_REQUEST_RESPONSE_CODE = 400;
	public static final int OK_RESPONSE_CODE = 200;
	public static final int USER_AUTHENTICATION_EXCEPTION_STATUS = 401;
	public static final int ALREADY_VERIFIED_EXCEPTION_STATUS = 201;
	public static final String RESETPASSWORD_URL = "http://localhost:8081/user/resetpassword/";
}
