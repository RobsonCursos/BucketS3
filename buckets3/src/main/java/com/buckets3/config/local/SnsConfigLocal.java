package com.buckets3.config.local;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.CreateTopicRequest;
import com.amazonaws.services.sns.model.Topic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * 	Test of SNS creation with LocalStack
 *  
 *  by Robson Costa [GFT - b3 - Credenciadoras]
 *  
 *  Date: 14-07-2022 
 *  
 */

@Configuration
@Profile("local")
public class SnsConfigLocal {
	private static final Logger LOG = LoggerFactory.getLogger(SnsConfigLocal.class);
	
	private final String s3AgendaEventsTopic;
	private final AmazonSNS snsClient;
	
	public SnsConfigLocal() {
		this.snsClient = AmazonSNSClient.builder()
				.withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration("http://localhost:4566",
									Regions.US_EAST_1.getName()))
				.withCredentials(new DefaultAWSCredentialsProviderChain())
				.build();
		
		CreateTopicRequest createTopicRequest = new CreateTopicRequest("s3-agenda-events");
		this.s3AgendaEventsTopic = this.snsClient.createTopic(createTopicRequest).getTopicArn();
		
		LOG.info("SNS topic ARN: {}", this.s3AgendaEventsTopic);
	}
	
	@Bean
	public AmazonSNS snsClient() {
		return this.snsClient;
	}
	
	@Bean
	@Qualifier("s3AgendaEventsTopic")
	public Topic snsProductEventsTopic() {
		return new Topic().withTopicArn(s3AgendaEventsTopic);
		
	}
}
