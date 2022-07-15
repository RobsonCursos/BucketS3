package com.buckets3.config.local;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.BucketNotificationConfiguration;
import com.amazonaws.services.s3.model.S3Event;
import com.amazonaws.services.s3.model.TopicConfiguration;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.CreateTopicRequest;
import com.amazonaws.services.sns.util.Topics;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.CreateQueueRequest;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * 	Test of Bucket S3 creation with LocalStack
 *  
 *  by Robson Costa [GFT - b3 - Credenciadoras]
 *  
 *  Date: 14-07-2022 
 *  
 */

@Configuration
@Profile("local")
public class S3ConfigLocal {
	private static final String BUCKET_NAME = "pcs-contratos";
	
	private AmazonS3 amazonS3;
	
	public S3ConfigLocal() {
		amazonS3 = getAmazonS3();
		
		createBucket();
		
		AmazonSNS snsClient = getAmazonSNS();
		
		String s3ContratoEventsTopicArn = createTopic(snsClient);
		
		AmazonSQS sqlClient = getAmazonSQS();
		
		createQueue(snsClient, s3ContratoEventsTopicArn, sqlClient);
		
		configureBucket(s3ContratoEventsTopicArn);
	}

	private AmazonS3 getAmazonS3() {
		AWSCredentials credentials = new BasicAWSCredentials("fakeAccessKeyId", "fakeSecretAccessKey");
		return AmazonS3Client.builder()
        		.withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration("http://localhost:4566",
						Regions.US_EAST_1.getName()))
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .enablePathStyleAccess()
                .build();

	}

	private AmazonSNS getAmazonSNS() {
		return  AmazonSNSClient.builder()
				.withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration("http://localhost:4566",
									Regions.US_EAST_1.getName()))
				.withCredentials(new DefaultAWSCredentialsProviderChain())
				.build();
	}

	private AmazonSQS getAmazonSQS() {
		return  AmazonSQSClient.builder()
				.withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration("http://localhost:4566",
									Regions.US_EAST_1.getName()))
				.withCredentials(new DefaultAWSCredentialsProviderChain())
				.build();
	}

	private void createBucket() {
		this.amazonS3.createBucket(BUCKET_NAME);
	}

	private String createTopic(AmazonSNS snsClient) {
		CreateTopicRequest createTopicRequest = new CreateTopicRequest("s3-contrato-events");
		return snsClient.createTopic(createTopicRequest).getTopicArn();
	}

	private void createQueue(AmazonSNS snsClient, String s3ContratoEventsTopicArn, AmazonSQS sqsClient) {

        String contratoEventsQueueUrl = sqsClient.createQueue(
                new CreateQueueRequest("s3-contrato-events")).getQueueUrl();

        Topics.subscribeQueue(snsClient, sqsClient, s3ContratoEventsTopicArn, contratoEventsQueueUrl);
	}

	private void configureBucket(String s3ContratoEventsTopicArn) {
		TopicConfiguration topicConfiguration = new TopicConfiguration();
		topicConfiguration.setTopicARN(s3ContratoEventsTopicArn);
		topicConfiguration.addEvent(S3Event.ObjectCreatedByPut);
		
		amazonS3.setBucketNotificationConfiguration(BUCKET_NAME,
					new BucketNotificationConfiguration().addConfiguration("putContrato", topicConfiguration));
	}

	@Bean
	public AmazonS3 amazonS3Client() {
		return this.amazonS3;
	}
	
	
}
