package com.bridgelabz.bookstore.response;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class Response {
	
	private int status;
	private String message;
	private Object data;
	private LocalDateTime time;
	
	public Response(int status,String message) {
		this.status = status;
		this.message = message;
	}
	
	public Response(String message,int status, Object data) {
		this.message = message;
		this.status = status;
		this.data = data;
	}
}
