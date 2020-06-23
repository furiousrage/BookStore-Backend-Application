package com.bridgelabz.bookstore.exception;

public class InvalidCredentialsException extends RuntimeException {	
	
	private static final long serialVersionUID = 1L;
	private int status;

	public InvalidCredentialsException(String message) {
		super(message);	
	}

	public InvalidCredentialsException(int status, String message) {
		super(message);	
		this.status= status;
	}

	public int getStatus() {
		return status;
	}
}