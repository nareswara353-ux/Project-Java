package com.example.enterprise.application.port;

import com.example.enterprise.domain.Product;

import java.util.List;

public interface ProductStatisticsService {
    long getTotalProducts();
    double getAveragePrice();
    int getTotalStock();
    Product getMostExpensiveProduct();
    Product getCheapestProduct();
}
