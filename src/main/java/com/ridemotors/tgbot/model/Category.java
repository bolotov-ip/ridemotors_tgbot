package com.ridemotors.tgbot.model;

import javax.persistence.*;

@Entity(name = "product_category")
public class Category implements Button{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    Long id;

    String name;

    Long parent = 0L;

    Long children = 0L;

    public Long getChildren() {
        return children;
    }

    public void setChildren(Long children) {
        this.children = children;
    }

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

    public Long getParent() {
        return parent;
    }

    public void setParent(Long parent) {
        this.parent = parent;
    }
}
