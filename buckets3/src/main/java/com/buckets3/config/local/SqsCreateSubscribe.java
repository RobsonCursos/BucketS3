package com.buckets3.config.local;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.Topic;
import com.amazonaws.services.sns.util.Topics;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * 	Test of subscription with LocalStack
 *  
 *  by Robson Costa [GFT - b3 - Credenciadoras]
 *  
 *  Date: 14-07-2022 
 *  
 */
@Configuration
@Profile("local")
public class SqsCreateSubscribe {

    public SqsCreateSubscribe(AmazonSNS snsClient, @Qualifier("s3AgendaEventsTopic") Topic s3AgendaEventsTopic) {
        AmazonSQS sqsClient = AmazonSQSClient.builder()
                .withEndpointConfiguration(
                        new AwsClientBuilder.EndpointConfiguration("http://localhost:4566",
                                Regions.US_EAST_1.getName()))
                .withCredentials(new DefaultAWSCredentialsProviderChain())
                .build();

        String s3AgendaEventsQueueUrl = sqsClient.createQueue(
                new CreateQueueRequest("s3-agenda-events")).getQueueUrl();

        Topics.subscribeQueue(snsClient, sqsClient, s3AgendaEventsTopic.getTopicArn(), s3AgendaEventsQueueUrl);
    }
}