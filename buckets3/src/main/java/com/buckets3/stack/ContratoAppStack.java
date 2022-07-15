package com.buckets3.stack;

import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.events.targets.SnsTopic;
import software.amazon.awscdk.services.s3.Bucket;
import software.amazon.awscdk.services.s3.EventType;
import software.amazon.awscdk.services.s3.notifications.SnsDestination;
import software.amazon.awscdk.services.sns.Topic;
import software.amazon.awscdk.services.sns.subscriptions.SqsSubscription;
import software.amazon.awscdk.services.sqs.DeadLetterQueue;
import software.amazon.awscdk.services.sqs.Queue;
import software.constructs.Construct;

/**
 * 	Bucket S3/SNS/SQS sample
 *  
 *  by Robson Costa [GFT - b3 - Credenciadoras]
 *  
 *  Date: 14-07-2022 
 *  
 */

public class ContratoAppStack extends Stack {

	private final Bucket bucket;
	private final Queue s3ContratoQueue;
		
	public ContratoAppStack(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public ContratoAppStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        SnsTopic s3ContratoTopic = SnsTopic.Builder.create(Topic.Builder.create(this, "S3ContratoTopic")
                .topicName("s3-contrato-events")
                .build())
                .build();

        bucket = Bucket.Builder.create(this, "S301")
                .bucketName("pcs-contratos")
                .removalPolicy(RemovalPolicy.DESTROY)
                .build();

        bucket.addEventNotification(EventType.OBJECT_CREATED_PUT, new SnsDestination(s3ContratoTopic.getTopic()));

        Queue s3ContratoDlq = Queue.Builder.create(this, "S3ContratoDlq")
                .queueName("s3-contrato-events-dlq")
                .build();

        DeadLetterQueue deadLetterQueue = DeadLetterQueue.builder()
                .queue(s3ContratoDlq)
                .maxReceiveCount(3)
                .build();

        s3ContratoQueue = Queue.Builder.create(this, "S3ContratoQueue")
                .queueName("s3-contrato-events")
                .deadLetterQueue(deadLetterQueue)
                .build();

        SqsSubscription sqsSubscription = SqsSubscription.Builder.create(s3ContratoQueue).build();
        s3ContratoTopic.getTopic().addSubscription(sqsSubscription);
    }

    public Bucket getBucket() {
        return bucket;
    }

    public Queue getS3InvoiceQueue() {
        return s3ContratoQueue;
    }
}
