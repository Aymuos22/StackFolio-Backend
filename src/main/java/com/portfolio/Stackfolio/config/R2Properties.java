package com.portfolio.Stackfolio.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "stackfolio.r2")
public class R2Properties {

    private String accountId = "";
    private String accessKeyId = "";
    private String secretAccessKey = "";
    private String bucket = "";
    private String publicBaseUrl = "";
    private long maxFileSizeBytes = 5_242_880L;

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    public String getSecretAccessKey() {
        return secretAccessKey;
    }

    public void setSecretAccessKey(String secretAccessKey) {
        this.secretAccessKey = secretAccessKey;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public String getPublicBaseUrl() {
        return publicBaseUrl;
    }

    public void setPublicBaseUrl(String publicBaseUrl) {
        this.publicBaseUrl = publicBaseUrl;
    }

    public long getMaxFileSizeBytes() {
        return maxFileSizeBytes;
    }

    public void setMaxFileSizeBytes(long maxFileSizeBytes) {
        this.maxFileSizeBytes = maxFileSizeBytes;
    }

    public boolean isConfigured() {
        return isPresent(accountId)
                && isPresent(accessKeyId)
                && isPresent(secretAccessKey)
                && isPresent(bucket);
    }

    public boolean hasPublicBaseUrl() {
        return isPresent(publicBaseUrl);
    }

    private boolean isPresent(String value) {
        return value != null && !value.isBlank();
    }
}
