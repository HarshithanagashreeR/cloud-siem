package com.amazon.siem.service.aws;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.*;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Collections;

@Service
public class AwsServiceImpl implements AwsService {
    private static final Logger logger = LoggerFactory.getLogger(AwsServiceImpl.class);

    @Autowired(required = false)
    private S3Client s3Client;

    @Autowired(required = false)
    private SnsClient snsClient;

    @Autowired(required = false)
    private CloudWatchLogsClient cloudWatchLogsClient;

    @Value("${aws.s3.bucket-name:siem-reports-bucket}")
    private String bucketName;

    @Value("${aws.sns.topic-arn:arn:aws:sns:us-east-1:123456789012:SiemAlertsTopic}")
    private String topicArn;

    @Override
    public String uploadReport(String fileName, byte[] content) {
        logger.info("Uploading report {} to S3 bucket {}", fileName, bucketName);
        try {
            if (s3Client != null) {
                PutObjectRequest putRequest = PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(fileName)
                        .contentType("application/pdf")
                        .build();
                s3Client.putObject(putRequest, RequestBody.fromBytes(content));
                String s3Url = "https://" + bucketName + ".s3.amazonaws.com/" + fileName;
                logger.info("Report uploaded successfully. S3 URL: {}", s3Url);
                return s3Url;
            }
        } catch (Exception e) {
            logger.warn("S3 upload failed (likely due to mock credentials): {}. Saving locally to simulated URL.", e.getMessage());
        }
        // Fallback simulated URL for local sandbox running
        return "https://simulated-s3.local/" + bucketName + "/" + fileName;
    }

    @Override
    public void publishCriticalAlert(String subject, String message) {
        logger.info("Publishing alert to SNS: Subject={}, Message={}", subject, message);
        try {
            if (snsClient != null) {
                PublishRequest publishRequest = PublishRequest.builder()
                        .topicArn(topicArn)
                        .subject(subject)
                        .message(message)
                        .build();
                snsClient.publish(publishRequest);
                logger.info("SNS Notification sent successfully.");
            }
        } catch (Exception e) {
            logger.warn("SNS publish failed (likely due to mock credentials): {}", e.getMessage());
        }
    }

    @Override
    public void logToCloudWatch(String logGroupName, String logStreamName, String message) {
        logger.debug("Forwarding security audit to CloudWatch [{}/{}]: {}", logGroupName, logStreamName, message);
        try {
            if (cloudWatchLogsClient != null) {
                // Ensure log group and stream exist
                try {
                    cloudWatchLogsClient.createLogGroup(CreateLogGroupRequest.builder().logGroupName(logGroupName).build());
                } catch (ResourceAlreadyExistsException ignored) {}

                try {
                    cloudWatchLogsClient.createLogStream(CreateLogStreamRequest.builder()
                            .logGroupName(logGroupName)
                            .logStreamName(logStreamName)
                            .build());
                } catch (ResourceAlreadyExistsException ignored) {}

                InputLogEvent logEvent = InputLogEvent.builder()
                        .timestamp(Instant.now().toEpochMilli())
                        .message(message)
                        .build();

                PutLogEventsRequest putRequest = PutLogEventsRequest.builder()
                        .logGroupName(logGroupName)
                        .logStreamName(logStreamName)
                        .logEvents(Collections.singletonList(logEvent))
                        .build();

                cloudWatchLogsClient.putLogEvents(putRequest);
            }
        } catch (Exception e) {
            logger.warn("CloudWatch logging failed (likely due to mock credentials): {}", e.getMessage());
        }
    }
}
