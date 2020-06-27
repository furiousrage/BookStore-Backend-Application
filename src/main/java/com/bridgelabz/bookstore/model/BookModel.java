package com.bridgelabz.bookstore.model;

import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Book")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BookModel {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "book_id")
	private Long bookId;

	@Column
	@NotNull
	private String bookName;

	@Column
	@NotNull
	private int quantity;

	@Column
	@NotNull
	private Double price;

	@Column
	@NotNull
	private String authorName;

	@CreationTimestamp
	private LocalDateTime createdDateAndTime;
	
	@UpdateTimestamp
	private LocalDateTime UpdatedDateAndTime;

//	@Column
//	private String image;

	@Column
	@NotNull
	private String bookDetails;

	@Column(nullable = false)
	private boolean isVerfied;
	
	@ManyToMany(cascade = CascadeType.ALL)
	@JoinColumn(name = "user_id")
	private List<UserModel> users;
	
	@ManyToMany(cascade = CascadeType.ALL)
	@JoinColumn(name = "seller_id")
	private List<SellerModel> sellers;
	
		
}