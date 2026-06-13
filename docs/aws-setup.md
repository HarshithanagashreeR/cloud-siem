# AWS Cloud Integration & IAM Guide

This guide details the configurations required in your AWS account to link the SIEM platform to AWS S3, SNS, and CloudWatch logs in production, adhering to Amazon IAM best security practices.

---

## 1. AWS IAM Least Privilege Policy

Create a custom IAM Role for the EC2 instance running the SIEM backend (or a service account if deploying via EKS/Kubernetes). Do **not** use administrator credentials. Apply the following policy:

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "S3ReportArchivePermissions",
      "Effect": "Allow",
      "Action": [
        "s3:PutObject",
        "s3:GetObject"
      ],
      "Resource": "arn:aws:s3:::siem-reports-bucket/*"
    },
    {
      "Sid": "SNSTopicPublishPermissions",
      "Effect": "Allow",
      "Action": [
        "sns:Publish"
      ],
      "Resource": "arn:aws:sns:us-east-1:123456789012:SiemAlertsTopic"
    },
    {
      "Sid": "CloudWatchLoggingPermissions",
      "Effect": "Allow",
      "Action": [
        "logs:CreateLogGroup",
        "logs:CreateLogStream",
        "logs:PutLogEvents"
      ],
      "Resource": "arn:aws:logs:us-east-1:123456789012:log-group:/aws/siem/*"
    }
  ]
}
```

---

## 2. AWS S3 Report Bucket Setup

1. **Create S3 Bucket:**
   * Name: `siem-reports-bucket` (must be unique).
   * Region: `us-east-1` (or your configured default region).
2. **Block Public Access:**
   * Keep **Block *all* public access** enabled to secure the PDFs.
3. **Configure SSE Encryption:**
   * Enable **Server-side encryption** using Amazon S3 managed keys (SSE-S3) or AWS KMS keys (SSE-KMS) to encrypt all archived PDF files at rest.
4. **Lifecycle Policy (Cost Management):**
   * Configure a lifecycle rule to automatically migrate files in `incident_reports/` and `system_reports/` to **Glacier Instant Retrieval** after 30 days, and permanently delete/expire logs after 365 days to minimize S3 costs.

---

## 3. AWS SNS Alerts Setup

1. **Create SNS Topic:**
   * Type: **Standard**
   * Name: `SiemAlertsTopic`
2. **Configure Subscription:**
   * Click **Create subscription**.
   * Protocol: **Email** (or SMS).
   * Endpoint: Add your security analyst team's email distribution list (e.g. `soc-alerts@company.com`).
3. **Confirm Subscription:**
   * Check your inbox and click the **Confirm Subscription** link in the mail sent by AWS.

---

## 4. AWS CloudWatch Logs Setup

* The application automatically streams security audit records.
* In the CloudWatch Console, under **Log Groups**, you will find the `/aws/siem/audit` log group.
* It is recommended to set a **Retention settings** policy (e.g., 90 days) on this log group to control CloudWatch Log Storage charges.
