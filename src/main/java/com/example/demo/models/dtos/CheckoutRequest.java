package com.example.demo.models.dtos;

import lombok.Data;

@Data
public class CheckoutRequest {
    // Client generates this (e.g. UUID). If same key sent within 10s,
    // returns the existing order instead of creating a duplicate.
    // If omitted, server generates one based on user + timestamp bucket.
    private String idempotencyKey;
}
