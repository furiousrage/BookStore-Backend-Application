package com.bridgelabz.bookstore.repository;

import java.awt.print.Book;
import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.beans.PropertyValues;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.bridgelabz.bookstore.model.CartModel;

@Repository
@Transactional
public interface CartRepository extends JpaRepository<CartModel, Long> {

	@Query(value = "select * from Cart where book_id=?", nativeQuery = true)
	Optional<CartModel> findByBookId(Long book_id);

	@Query(value = "select * from Cart where user_id=?", nativeQuery = true)
	List<CartModel> findByUserId(Long userId);

	@Query(value = "select * from Cart where book_id=? and user_id=?", nativeQuery = true)
	Optional<CartModel> findByBookIdAndUserId(Long book_id,Long user_id);

    boolean existsByBookIdAndUserId(Long bookId, long id);

    List<CartModel> findAllByUserId(long id);

void deleteAllByUserId(long id);

    //@Query(value="delete * from Cart where book_id=?" ,nativeQuery = true)
	//Optional<CartModel> removeAllItem(Long bookId);

	//CartModel findByBookId(Long book_id);
}
