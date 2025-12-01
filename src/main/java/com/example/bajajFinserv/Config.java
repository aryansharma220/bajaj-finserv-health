package com.example.bajajFinserv;

import org.springframework.stereotype.Component;
import org.springframework.boot.context.properties.ConfigurationProperties;


@Component
@ConfigurationProperties(prefix = "bajaj")
public class Config {

    private String name;
    private String regNo;
    private String email;
    private String baseUrl;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRegNo() {
        return regNo;
    }

    public void setRegNo(String regNo) {
        this.regNo = regNo;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
}