package com.bridgelabz.bookstore.serviceimplementation;

import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.bridgelabz.bookstore.exception.UserException;
import com.bridgelabz.bookstore.model.UserModel;
import com.bridgelabz.bookstore.repository.UserRepository;
import com.bridgelabz.bookstore.service.AmazonS3ClientService;
import com.bridgelabz.bookstore.utility.JwtGenerator;

//import com.springbootdev.amazon.s3.example.aws.service.AmazonS3ClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

@Component
public class AmazonS3ClientServiceImpl implements AmazonS3ClientService
{
	@Autowired
	JwtGenerator jwt;
	
	@Autowired
	UserRepository userRepository;
	
    private String awsS3AudioBucket;
    private AmazonS3 amazonS3;
    private static final Logger logger = LoggerFactory.getLogger(AmazonS3ClientServiceImpl.class);

    @Autowired
    public AmazonS3ClientServiceImpl(Region awsRegion, AWSCredentialsProvider awsCredentialsProvider, String awsS3AudioBucket) 
    {
        this.amazonS3 = AmazonS3ClientBuilder.standard()
                .withCredentials(awsCredentialsProvider)
                .withRegion(awsRegion.getName()).build();
        this.awsS3AudioBucket = awsS3AudioBucket;
    }
@Async
    public UserModel uploadFileToS3Bucket(MultipartFile multipartFile, boolean enablePublicReadAccess,String token) throws UserException 
    {
    	Long userid=jwt.decodeJWT(token);
    	UserModel user=userRepository.findUserById(userid).orElseThrow(() ->new UserException("User Not Found"));
    	String fileName = multipartFile.getOriginalFilename();
       String profile="https://"+awsS3AudioBucket+".s3.ap-south-1.amazonaws.com/"+fileName;
        user.setProfile(profile);
    	    try {
            //creating the file in the server (temporarily)
            File file = new File(fileName);
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(multipartFile.getBytes());
            fos.close();
          
            PutObjectRequest putObjectRequest = new PutObjectRequest(this.awsS3AudioBucket, fileName, file);

            if (enablePublicReadAccess) {
                putObjectRequest.withCannedAcl(CannedAccessControlList.PublicRead);
            }
            this.amazonS3.putObject(putObjectRequest);
            userRepository.save(user);
            //removing the file created in the server
            file.delete();
            
            return user;
        } catch (IOException | AmazonServiceException ex) {
            logger.error("error [" + ex.getMessage() + "] occurred while uploading [" + fileName + "] ");
        }
		return user;
       
    }

    @Async
    public void deleteFileFromS3Bucket(String fileName) 
    {
        try {
            amazonS3.deleteObject(new DeleteObjectRequest(awsS3AudioBucket, fileName));
        } catch (AmazonServiceException ex) {
            logger.error("error [" + ex.getMessage() + "] occurred while removing [" + fileName + "] ");
        }
    }
}
