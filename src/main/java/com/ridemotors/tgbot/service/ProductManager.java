package com.ridemotors.tgbot.service;

import com.ridemotors.tgbot.constant.STATE_ADD_PRODUCT;
import com.ridemotors.tgbot.domain.DocumentRead;
import com.ridemotors.tgbot.util.ExcelManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class ProductManager {

    @Autowired
    ExcelManager excelManager;

    public STATE_ADD_PRODUCT addAllProduct(File file) {
        DocumentRead documentRead = excelManager.parse(file);
        return STATE_ADD_PRODUCT.SUCCESS;
    }

}
