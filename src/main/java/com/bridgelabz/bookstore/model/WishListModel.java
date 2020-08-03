package com.bridgelabz.bookstore.model;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Data
@Entity
public class WishListModel {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private long userId;
    private long bookId;
    private String bookName;
    private String authorName;
    private String bookImgUrl;
    private double price;
    private int maxQuantity;
    private int quantity;
}
