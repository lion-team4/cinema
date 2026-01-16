package com.example.cinema.config.cloudFront;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cloudfront")
public class CloudFrontProperties {

    private String domain;
    private String originPathPrefix = "";

    public String getDomain() { return domain; }
    public void setDomain(String domain) { this.domain = domain; }

    public String getOriginPathPrefix() { return originPathPrefix; }
    public void setOriginPathPrefix(String originPathPrefix) { this.originPathPrefix = originPathPrefix; }
}