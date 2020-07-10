package com.bridgelabz.bookstore.repository;

import java.awt.print.Book;
import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.bridgelabz.bookstore.model.CartModel;

@Repository
@Transactional
public interface CartRepository extends JpaRepository<CartModel, Long> {

	@Query(value = "select * from Cart where book_id=?", nativeQuery = true)
	Optional<CartModel> findByBookId(Long book_id);
	
	//@Query(value="delete * from Cart where book_id=?" ,nativeQuery = true)
	//Optional<CartModel> removeAllItem(Long bookId);

	//CartModel findByBookId(Long book_id);

}
