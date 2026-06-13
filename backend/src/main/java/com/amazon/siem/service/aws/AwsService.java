package com.amazon.siem.service.aws;

public interface AwsService {
    String uploadReport(String fileName, byte[] content);
    void publishCriticalAlert(String subject, String message);
    void logToCloudWatch(String logGroupName, String logStreamName, String message);
}
