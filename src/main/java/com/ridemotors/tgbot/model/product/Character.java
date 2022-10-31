package com.ridemotors.tgbot.model.product;

import javax.persistence.*;

@Entity(name = "character")
public class Character {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    Long id;

    String name;

    Long productTypeId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getProductTypeId() {
        return productTypeId;
    }

    public void setProductTypeId(Long productTypeId) {
        this.productTypeId = productTypeId;
    }
}
