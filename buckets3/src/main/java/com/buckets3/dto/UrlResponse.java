package com.buckets3.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UrlResponse {
    private String url;
    private long expirationTime;
}