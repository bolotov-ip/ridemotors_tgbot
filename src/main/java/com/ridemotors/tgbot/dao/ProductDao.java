package com.ridemotors.tgbot.dao;

import com.ridemotors.tgbot.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface  ProductDao extends JpaRepository<Product, Long> {

    @Query("SELECT p FROM product p where p.typeId =:typeId")
    public List<Product> getProductByType(@Param("typeId") Long typeId);

    default public void removeAllProductByType(Long typeId) {
        List<Product> products = getProductByType(typeId);
        deleteAll(products);
    }

    default public void sourceAdded(Long productId) {
        Optional<Product> product = findById(productId);
        if(product.isPresent()){
            Product prod = product.get();
            prod.setSource(true);
            save(prod);
        }
    }
}
