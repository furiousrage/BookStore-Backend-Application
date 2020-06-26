package com.bridgelabz.bookstore.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.bridgelabz.bookstore.model.UserModel;

@Repository
@Transactional
public interface UserRepository extends JpaRepository<UserModel, Long> {

	@Query(value="Select * from user where email_id = :emailId",nativeQuery = true)
	UserModel findEmail(String emailId);
	
	@Query(value="Select * from user",nativeQuery = true)
	List<UserModel> getAllUsers();
	
	@Query(value = "select * from user where emailId=?", nativeQuery = true)
	UserModel findByEmail(String emailId);
	
	@Query(value = "select * from user where emailId=?", nativeQuery = true)
	Optional<UserModel> findByEmailId(String emailId);
	
	@Query(value = "select * from user where user_id = :userId", nativeQuery = true)
	UserModel findById(long userId);
	
	@Query(value = "select * from user where user_id = :userId", nativeQuery = true)
	Optional<UserModel> findUserById(long userId);

	@Modifying
	@Query(value="Insert into user(full_name, email_id, mobile_number, password, is_verified, registered_at, updated_at, role_type) values (:fullName,:emailId, :mobileNumber, :password,:isVerified,:registeredAt,:updatedAt, :roleType)",nativeQuery = true)
	void insertdata(String fullName, String emailId,String mobileNumber, String password ,boolean isVerified, LocalDateTime registeredAt, LocalDateTime updatedAt, Enum roleType);

	@Modifying
	@Query(value="update user set is_verified = true where user_id = :userId", nativeQuery = true)
	void verify(long userId);

	@Modifying
	@Query(value="update user set updated_at = now() where user_id = :userId", nativeQuery = true)
	void updatedAt(long userId);
}
