package com.ridemotors.tgbot.service;

import com.ridemotors.tgbot.domain.ProductsReadable;
import com.ridemotors.tgbot.exception.FormatExcelException;
import com.ridemotors.tgbot.util.Util;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class ExcelManager {

    private enum TYPE_ROW {
        PRODUCT, HEADER, EMPTY, COMMAND_ADD_PRODUCT, COMMAND_DELETE_PRODUCT, COMMAND_IGNORE_ROW;
    }

    private final Logger log = LoggerFactory.getLogger(ExcelManager.class);

    public ProductsReadable parseProducts(File file) throws IOException, FormatExcelException {
        log.info("Считывание файла {} {}", file.getName(), new Date(System.currentTimeMillis()));
        Workbook workbook = new XSSFWorkbook(new FileInputStream(file));
        Sheet sheet = workbook.getSheetAt(0);
        List<String> columnNames = null;
        List<HashMap<String, String>> productsAdd = new ArrayList();
        List<HashMap<String, String>> productsDelete = new ArrayList();
        TYPE_ROW command = TYPE_ROW.COMMAND_ADD_PRODUCT;
        for (Row row : sheet) {
            switch (getTypeRow(row)){
                case EMPTY:
                    continue;
                case COMMAND_IGNORE_ROW:
                    command = TYPE_ROW.COMMAND_IGNORE_ROW;
                    break;
                case COMMAND_ADD_PRODUCT:
                    command = TYPE_ROW.COMMAND_ADD_PRODUCT;
                    break;
                case COMMAND_DELETE_PRODUCT:
                    command = TYPE_ROW.COMMAND_DELETE_PRODUCT;
                    break;
                case HEADER:
                    columnNames = getColumnNames(row);
                    break;
                case PRODUCT:
                    if(command.equals(TYPE_ROW.COMMAND_ADD_PRODUCT))
                        productsAdd.add(getProductData(row, columnNames));
                    if(command.equals(TYPE_ROW.COMMAND_DELETE_PRODUCT))
                        productsDelete.add(getProductData(row, columnNames));
                    break;
            }
        }
        ProductsReadable productsReadable = new ProductsReadable();
        productsReadable.setProductsAdd(productsAdd);
        productsReadable.setProductsDelete(productsDelete);
        log.info("Считывание файла {} завершено успешно", file.getName());
        workbook.close();
        return productsReadable;
    }

    private HashMap<String, String> getProductData(Row row, List<String> columnNames) throws FormatExcelException {
        if(columnNames==null)
            throw new FormatExcelException("Необходимо указать наименование полей для каждой отдельной категории товаров");
        HashMap<String,String> productData = new HashMap<>();
        int column = 0;
        for(Cell cell : row) {
            String cellValue = getCellValue(cell);
            if(column<columnNames.size())
                productData.put(columnNames.get(column), cellValue);
            else break;
            column++;
        }
        return productData;
    }

    private List<String> getColumnNames(Row row) {
        List<String> columnNames = new ArrayList();
        for(Cell cell : row) {
            if(cell!=null) {
                String cellValue = getCellValue(cell);
                if(!cellValue.isBlank())
                    columnNames.add(cellValue);
            }
        }
        return columnNames;
    }

    private String getCellValue(Cell cell) {
        String cellValue = "";
        switch (cell.getCellType()){
            case STRING:
                cellValue = cell.getStringCellValue();
                break;
            case NUMERIC:
                cellValue = Util.doubleToString(cell.getNumericCellValue());
        }
        return cellValue;
    }

    private TYPE_ROW getTypeRow(Row row) throws FormatExcelException {
        if(isEmptyRow(row))
            return TYPE_ROW.EMPTY;
        Cell firstCell = row.getCell(0);
        if(firstCell!=null) {
            boolean isString = firstCell.getCellType().equals(CellType.STRING);
            boolean isNumeric = firstCell.getCellType().equals(CellType.NUMERIC);
            if(isString) {
                boolean isNotBlank = !firstCell.getStringCellValue().isBlank();
                if(isNotBlank) {
                    String cellValue = firstCell.getStringCellValue();
                    switch(cellValue.toLowerCase()) {
                        case "добавить товар":
                            return TYPE_ROW.COMMAND_ADD_PRODUCT;
                        case "id_category*":
                        case "id*":
                            return TYPE_ROW.HEADER;
                        case "удалить товар":
                            return TYPE_ROW.COMMAND_DELETE_PRODUCT;
                        case "примечание*":
                            return TYPE_ROW.COMMAND_IGNORE_ROW;
                        default:
                            return TYPE_ROW.PRODUCT;
                    }
                }
            }
            if(isNumeric) {
                return TYPE_ROW.PRODUCT;
            }
        }
        return TYPE_ROW.EMPTY;
    }

    private boolean isEmptyRow(Row row) {
        for(Cell cell : row) {
            switch (cell.getCellType()){
                case NUMERIC:
                    return false;
                case STRING:
                    if(cell.getStringCellValue()!=null && !cell.getStringCellValue().isBlank())
                        return false;
            }
        }
        return true;
    }

    public File generateExcel(ProductsReadable doc) {
        return null;
    }
}
