package com.bridgelabz.bookstore.service;

import java.util.List;

import com.bridgelabz.bookstore.model.BookModel;

public interface ElasticSearchService {

	String addBook(BookModel bookModel);
	
}
