package com.ridemotors.tgbot.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ridemotors.tgbot.constant.STATE_UPDATE_PRODUCT;
import com.ridemotors.tgbot.dao.ProductDao;
import com.ridemotors.tgbot.domain.ProductsReadable;
import com.ridemotors.tgbot.exception.AddProductException;
import com.ridemotors.tgbot.exception.FormatExcelException;
import com.ridemotors.tgbot.model.Product;
import com.ridemotors.tgbot.util.ExcelManager;
import com.ridemotors.tgbot.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private final Logger log = LoggerFactory.getLogger(ProductManager.class);

    public File getFileProducts(Long rootCategory) {
        List<Product> products = null;
        return null;
    }

    public List<Product> getCategoryProducts(Long id) {
        List<Product> products = productDao.findByCategory(id);
        if(products==null)
            return new ArrayList<>();
        return products;
    }

    public STATE_UPDATE_PRODUCT updateProducts(File file) {
        STATE_UPDATE_PRODUCT answer = STATE_UPDATE_PRODUCT.SUCCESS;
        try {
            ProductsReadable productsReadable = excelManager.parseProducts(file);
            addProducts(productsReadable.getProductsAdd());
            List<Long> idListNotFound = deleteProducts(productsReadable.getProductsDelete());
            if(idListNotFound.size()>0) {
                answer = STATE_UPDATE_PRODUCT.WARNING;
                answer.setIdListNotFound(idListNotFound);
            }
        } catch (FormatExcelException|AddProductException e) {
            answer = STATE_UPDATE_PRODUCT.FAILED;
            answer.setTextFailed(e.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
        return answer;
    }

    private void addProducts(List<HashMap<String, String>> productsAdd) throws AddProductException {
        STATE_UPDATE_PRODUCT state = STATE_UPDATE_PRODUCT.SUCCESS;
        List<Product> products = new ArrayList<>();
        for(HashMap<String, String> product : productsAdd) {
            try {
                products.add(createProduct(product));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        productDao.saveAll(products);
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

    private Product createProduct(HashMap<String, String> productData) throws JsonProcessingException, AddProductException {
        Long idCategory = Util.formatStringToLong(productData.get("id_category*"));
        Long id = Util.formatStringToLong(productData.get("id*"));

        Product product = id==0? new Product() : productDao.findById(id).isPresent() ? productDao.findById(id).get() : null;
        if(product==null)
            throw new AddProductException("Товар с таким id не найден " +  id);
        product.setCategory(idCategory);
        product.setDescription(productData.get("Описание*"));
        product.setName(productData.get("Наименование*"));
        product.setPrice(productData.get("Цена*"));
        // Удаляем все обязательные и справочные столбцы чтобы воспользоваться ObjectMapper
        List<String> keysRemove = new ArrayList<>();
        for(Map.Entry<String, String> entry : productData.entrySet()) {
            String key = entry.getKey();
            if(key.contains("*"))
                keysRemove.add(key);
        }
        for(String key : keysRemove)
            productData.remove(key);

        // Убеждаемся что в таблице в поле character те же заголовки
        // !!!!!!!!!!!!! Надо написать метод для получения одного продукта , а не вытаскивать все !!!!!!!!!!!!!!!!!!
        List<Product> productsCategory = productDao.findByCategory(idCategory);
        Map<String, String> map = Util.convertStringToMap(productsCategory.get(0).getCharacter());
        Set<String> keySet = map.keySet();
        if(!map.keySet().equals(productData.keySet()))
            throw new AddProductException("Дополнительные поля добавляемого товара имеют " +
                    "несовпадающий заголовок с товарами этой категории");

        product.setCharacter(Util.convertMapToString(productData));
        return product;
    }
}
