package com.whataburger.whataburgerproject.domain;

import com.whataburger.whataburgerproject.exception.NotEnoughStockException;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
public class Product {
    @Id
    @Column(name = "product_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private int price;
    private int calories;
//    private int stock;
    @OneToMany(mappedBy = "product")
    private List<ProductIngredient> productIngredients = new ArrayList<>();

//    public void addStock(int quantity) {
//        this.stock += quantity;
//    }
//
//    public void reduceStock(int quantity) {
//         int reducedStock = this.stock - quantity;
//         if (reducedStock < 0) {
//             throw new NotEnoughStockException("not enough stock");
//         }
//         this.stock = reducedStock;
//    }
}
