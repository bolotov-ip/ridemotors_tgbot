package com.ridemotors.tgbot.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ridemotors.tgbot.constant.STATE_ADD_PRODUCT;
import com.ridemotors.tgbot.dao.ProductDao;
import com.ridemotors.tgbot.domain.ProductsReadable;
import com.ridemotors.tgbot.exception.FormatExcelException;
import com.ridemotors.tgbot.model.Product;
import com.ridemotors.tgbot.util.ExcelManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Service
public class ProductManager {

    @Autowired
    ExcelManager excelManager;

    @Autowired
    ProductDao productDao;

    // Добавить поля в STATE_ADD_PRODUCT idFailed
    public STATE_ADD_PRODUCT manageProducts(File file) {
        STATE_ADD_PRODUCT answer = null;
        try {
            ProductsReadable productsReadable = excelManager.parseProducts(file);
            addProducts(productsReadable.getProductsAdd());
            List<Long> idFailed = deleteProducts(productsReadable.getProductsDelete());

        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (FormatExcelException e) {
            throw new RuntimeException(e);
        }
        answer = STATE_ADD_PRODUCT.SUCCESS;
        return answer;
    }
    // Изменить exception
    private STATE_ADD_PRODUCT addProducts(List<HashMap<String, String>> productsAdd) {
        STATE_ADD_PRODUCT state = STATE_ADD_PRODUCT.SUCCESS;
        List<Product> products = new ArrayList<>();
        for(HashMap<String, String> product : productsAdd) {
            try {
                products.add(createProduct(product));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        productDao.saveAll(products);
        return state;
    }

    private List<Long> deleteProducts(List<HashMap<String, String>> productsDelete) {
        List<Long> idVerified = new ArrayList<>();
        List<Long> idFailed = new ArrayList<>();
        for(HashMap<String, String> productData : productsDelete) {
            Long id = Long.valueOf(productData.get("id*"));
            Optional<Product> productOptional = productDao.findById(id);
            if(productOptional.isPresent())
                idVerified.add(productOptional.get().getId());
            else
                idFailed.add(id);
        }
        productDao.deleteAllById(idVerified);
        return idFailed;
    }

    private Product createProduct(HashMap<String, String> productData) throws JsonProcessingException {
        Product product = new Product();
        product.setCategory(Long.valueOf(productData.get("id_category*")));
        product.setDescription(productData.get("Описание*"));
        product.setName(productData.get("Наименование*"));
        product.setPrice(Long.valueOf(productData.get("Цена*")));
        // Удаляем все обязательные и справочные столбцы
        for(Map.Entry<String, String> entry : productData.entrySet()) {
            String key = entry.getKey();
            if(key.contains("*"))
                productData.remove(key);
        }
        // Все необязательные столбцы хранятся как json, чтобы товар с разными характеристиками
        // мог хранится в одной таблице
        ObjectMapper objectMapper = new ObjectMapper();
        if(productData.size()>0) {
            String characters = objectMapper.writeValueAsString(productData);
            product.setCharacter(characters);
        }
        return product;
    }
}
