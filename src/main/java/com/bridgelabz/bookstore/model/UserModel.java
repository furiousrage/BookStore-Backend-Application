package com.bridgelabz.bookstore.model;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "user")
public class UserModel {

	  @Id
	  @GeneratedValue(strategy = GenerationType.IDENTITY)
	  @Column(name = "user_id")
      private long userId;
	   
	  @Column(name = "full_name", nullable = false)
      private String fullName;
	   
	  @Column(name = "email_id", unique = true, nullable = false)
      private String emailId;
      
	  @Column(name = "mobile_number", unique = true, length = 10, nullable = false)
      private Long mobileNumber;
      
      @Column(name = "password", nullable = false, unique= true)
      private String password;
      
      @Column(columnDefinition = "boolean default false")
      private boolean isVerified;
      
      @Column(name = "registered_at")
  	  public LocalDateTime registeredAt;

  	  @Column(name = "updated_at")
  	  public LocalDateTime updatedAt;
  	  
  	  @Column(columnDefinition = "boolean default false")
  	  public boolean userStatus;
  	  
  	  public UserModel(String fullName, String emailId, Long mobileNumber, String password) {
		super();
		this.fullName = fullName;
		this.password = password;
		this.mobileNumber = mobileNumber;
		this.emailId = emailId;
  	  }
}
