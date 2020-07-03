package com.bridgelabz.bookstore.service;

import org.springframework.web.multipart.MultipartFile;

import com.bridgelabz.bookstore.exception.UserException;
import com.bridgelabz.bookstore.model.UserModel;


public interface AmazonS3ClientService
{
    UserModel uploadFileToS3Bucket(MultipartFile multipartFile, boolean enablePublicReadAccess,String token)throws UserException;

    void deleteFileFromS3Bucket(String fileName);
}
