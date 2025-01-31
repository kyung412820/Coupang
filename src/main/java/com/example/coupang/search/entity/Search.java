package com.example.coupang.search.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class Search {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String searchText;

    @Column
    @NotNull
    private Long count;

    public Search(Long id, String searchText, Long count) {
        this.id = id;
        this.searchText = searchText;
        this.count = count;
    }
}
