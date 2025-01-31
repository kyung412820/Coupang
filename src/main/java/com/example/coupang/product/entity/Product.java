package com.example.coupang.product.entity;


import com.example.coupang.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @NotBlank
    @Column(length = 20)
    private String productName;

    @NotBlank
    @Column(length = 100)
    private String contents;

    @NotNull
    private Long price;



    public Product(User user, String productName, String contents, Long price) {
        this.user = user;
        this.productName = productName;
        this.contents = contents;
        this.price = price;
    }

}
