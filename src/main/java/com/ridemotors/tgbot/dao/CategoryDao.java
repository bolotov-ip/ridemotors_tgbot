package com.ridemotors.tgbot.dao;

import com.ridemotors.tgbot.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CategoryDao extends JpaRepository<Category, Long> {

    @Query("SELECT pc FROM product_category pc where pc.parent =:parent order by pc.id")
    public List<Category> getCategoryByParent(@Param("parent") Long parent);

    default public void deleteById(Long categoryId) {
        Optional<Category> category = findById(categoryId);
        if(category.isPresent())
            delete(category.get());
    }

}
