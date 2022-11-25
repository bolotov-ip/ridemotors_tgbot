package com.ridemotors.tgbot.service;

import com.ridemotors.tgbot.dao.CategoryDao;
import com.ridemotors.tgbot.model.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CategoryManager {

    @Autowired
    CategoryDao categoryDao;

    public Category getParentCategory(Long id) {
        Category category = getCategory(id);
        Long parentId = category.getParent();
        Category parentCategory = getCategory(parentId);
        return parentCategory;
    }

    public Category getCategory(Long id) {
        Optional<Category> category = categoryDao.findById(id);
        if(category.isPresent())
            return category.get();
        return null;
    }

    public boolean existCategory(Long id) {
        Optional<Category> category = categoryDao.findById(id);
        if(category.isPresent())
            return true;
        return false;
    }

    public String getCategoryName(Long id) {
       Optional<Category> category = categoryDao.findById(id);
       if(category.isPresent())
           return category.get().getName();
       else
           return "";
    }

    public List<Category> getAllChildren(Long categoryId) {
        List<Category> result = new ArrayList<>();
        List<Category> categories = categoryDao.getCategoryByParent(categoryId);
        if(categories == null)
            return new ArrayList<>();
        else {
            result.addAll(categories);
            for(Category category : categories) {
                result.addAll(getAllChildren(category.getId()));
            }
        }
        return result;
    }

    public List<Category> getChildren(Long categoryId) {
        List<Category> categories = categoryDao.getCategoryByParent(categoryId);
        if(categories == null)
            return new ArrayList<>();
        return categories;
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
        category = categoryDao.save(category);
        Optional<Category> parentCategory = categoryDao.findById(parentId);
        if(parentCategory.isPresent()) {
            parentCategory.get().setChildren(category.getId());
            categoryDao.save(parentCategory.get());
        }

    }
}
