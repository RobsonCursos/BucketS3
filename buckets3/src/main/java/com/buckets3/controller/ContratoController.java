package com.buckets3.controller;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.buckets3.dto.UrlResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@RestController
@RequestMapping("/api/contrato")
public class ContratoController {

    @Value("${aws.s3.bucket.contrato.name}")
    private String bucketName;

    private AmazonS3 amazonS3;
    
    @Autowired
    public ContratoController(AmazonS3 amazonS3) {
        this.amazonS3 = amazonS3;
    }

    @PostMapping
    //URL Pre-assinada
    public ResponseEntity<UrlResponse> createContratoUrl() {

    	UrlResponse urlResponse  = new UrlResponse();
        Instant expirationTime = Instant.now().plus(Duration.ofMinutes(5));
        String processId = UUID.randomUUID().toString();

        GeneratePresignedUrlRequest generatePresignedUrlRequest =
                new GeneratePresignedUrlRequest(bucketName, processId)
                        .withMethod(HttpMethod.PUT)
                        .withExpiration(Date.from(expirationTime));

        urlResponse.setExpirationTime(expirationTime.getEpochSecond());
        urlResponse.setUrl(amazonS3.generatePresignedUrl(
                generatePresignedUrlRequest).toString());

        return new ResponseEntity<UrlResponse>(urlResponse, HttpStatus.OK);
    }
}

























