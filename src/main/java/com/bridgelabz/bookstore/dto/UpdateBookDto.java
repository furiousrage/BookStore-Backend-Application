package com.bridgelabz.bookstore.dto;

import org.springframework.stereotype.Component;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Component
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBookDto {
	 private String bookName;

	    private int quantity;
		
		private Double price;
		
		private String authorName;
		
//		private String image;
		
		private String bookDetails;
		
	}
