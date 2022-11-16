package com.ridemotors.tgbot.domain;

import java.util.HashMap;
import java.util.List;

public class ProductsReadable {

    List<HashMap<String, String>> productsAdd;
    List<HashMap<String, String>> productsDelete;

    public List<HashMap<String, String>> getProductsDelete() {
        return productsDelete;
    }

    public void setProductsDelete(List<HashMap<String, String>> productsDelete) {
        this.productsDelete = productsDelete;
    }

    public List<HashMap<String, String>> getProductsAdd() {
        return productsAdd;
    }

    public void setProductsAdd(List<HashMap<String, String>> productsAdd) {
        this.productsAdd = productsAdd;
    }
}
