package com.example.demo.exception;

import lombok.Getter;

import java.util.List;

@Getter
public class StockConflictException extends RuntimeException {

    private final List<String> stockErrors;

    public StockConflictException(List<String> stockErrors) {
        super("One or more products failed stock verification");
        this.stockErrors = stockErrors;
    }
}
