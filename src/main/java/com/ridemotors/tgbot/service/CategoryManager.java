package com.ridemotors.tgbot.service;

import com.ridemotors.tgbot.constant.STATE_UPDATE_CATEGORY;
import com.ridemotors.tgbot.dao.CategoryDao;
import com.ridemotors.tgbot.model.Category;
import org.checkerframework.checker.units.qual.C;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CategoryManager {

    @Autowired
    CategoryDao categoryDao;

    public String getCategoryName(Long id) {
       Optional<Category> category = categoryDao.findById(id);
       if(category.isPresent())
           return category.get().getName();
       else
           return "";
    }

    public List<Category> getChildren(Long category) {
        List<Category> result = categoryDao.getCategoryByParent(category);
        if(result == null)
            return new ArrayList<>();
        return result;
    }

    public List<Category> getAllChildren(Long category) {
        return null;
    }

    public STATE_UPDATE_CATEGORY addCategory(Long parent, String name) {
        return STATE_UPDATE_CATEGORY.SUCCESS;
    }

    public void deleteCategory(Long id, ProductManager productManager) {
        Optional<Category> categoryOptional = categoryDao.findById(id);
        if(categoryOptional.isPresent()){
            Category category = categoryOptional.get();
            List<Category> childrenCategories = getChildren(category.getId());
            for(Category childCategory : childrenCategories) {
                deleteCategory(childCategory.getId(), productManager);
            }
            productManager.deleteProductsByCategory(category.getId());
            categoryDao.delete(category);
        }
    }

    public void createCategory(Long parentId, String name) {
        Category category = new Category();
        category.setParent(parentId);
        category.setName(name);
        categoryDao.save(category);
    }
}
