package com.ridemotors.tgbot.dao.product;

import com.ridemotors.tgbot.model.product.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CategoryDao extends JpaRepository<Category, Long> {

    @Query("SELECT pc FROM product_category pc where pc.parentCategoryId =:parentCategoryId")
    public List<Category> getCategoryByParent(@Param("parentCategoryId") Long parentCategoryId);

    default public void deleteById(Long categoryId) {
        Optional<Category> category = findById(categoryId);
        if(category.isPresent())
            delete(category.get());
    }

}
