package com.ridemotors.tgbot.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ridemotors.tgbot.domain.ProductsReadable;
import com.ridemotors.tgbot.exception.FormatExcelException;
import com.ridemotors.tgbot.model.Product;
import com.ridemotors.tgbot.util.Util;
import com.ridemotors.tgbot.util.UtilFile;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Date;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class ExcelManager {

    @Autowired
    CategoryManager categoryManager;

    @Autowired
    ResourceManager resourceManager;

    @Autowired
    UtilFile utilFile;

    private enum TYPE_ROW {
        PRODUCT, HEADER, EMPTY, COMMAND_ADD_PRODUCT, COMMAND_DELETE_PRODUCT, COMMAND_IGNORE_ROW;
    }

    private final Logger log = LoggerFactory.getLogger(ExcelManager.class);

    public File createExcel(List<Product> products, boolean isAdmin) throws IOException {
        int columnWidth = 8000;
        List<Integer> widthListAdmin = Arrays.asList(1500, 4000, 4000, 6000, 10000, 1500, 2000, 2000);
        List<Integer> widthListUser = Arrays.asList(4000, 6000, 10000, 1500);
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Товары");
        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        CellStyle style = workbook.createCellStyle();
        style.setWrapText(true);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        List<String> columnNameCommon = new ArrayList<>();
        if(isAdmin) {
            columnNameCommon.add("id*");
            columnNameCommon.add("id_category*");
        }
        columnNameCommon.add("Наименование*");
        columnNameCommon.add("Наименование категории**");
        columnNameCommon.add("Описание*");
        columnNameCommon.add("Цена*");
        if(isAdmin) {
            columnNameCommon.add("Фото**");
            columnNameCommon.add("Видео**");
        }

        List<String> columnName = new ArrayList<>();

        int countRow = 0;
        boolean isHeader = false;
        for(Product product : products) {
            if(product==null){
                isHeader = true;
                countRow++;
                continue;
            }
            if(isHeader) {
                columnName= new ArrayList<>(columnNameCommon);
                Map<String,String> characters = Util.convertStringToMap(product.getCharacter());
                for(Map.Entry<String,String> entry : characters.entrySet()) {
                    columnName.add(entry.getKey());
                }
                Row header = sheet.createRow(countRow);
                for(int i=0; i<columnName.size(); i++) {
                    List<Integer> listColumnWidth = isAdmin ? widthListAdmin : widthListUser;
                    if(i<listColumnWidth.size())
                        sheet.setColumnWidth(i, listColumnWidth.get(i));
                    else
                        sheet.setColumnWidth(i, columnWidth);
                    Cell cell =  header.createCell(i);
                    cell.setCellStyle(headerStyle);
                    cell.setCellValue(columnName.get(i));
                }
                isHeader=false;
                countRow++;
            }
            Row row = sheet.createRow(countRow);
            Map<String, String> rowData = getRowData(columnName, product, isAdmin);
            for(int i=0; i<columnName.size(); i++) {
                Cell cell =  row.createCell(i);
                cell.setCellStyle(style);
                cell.setCellValue(rowData.get(columnName.get(i)));
            }
            countRow++;
        }
        DateTimeFormatter format = DateTimeFormatter.ofPattern("dd-MM-yyyy HH-mm-ss");
        String formatDateTime = LocalDateTime.now().format(format);
        String fileLocation = utilFile.getPathTemp() + "Товары от " + formatDateTime + ".xlsx";
        FileOutputStream outputStream = new FileOutputStream(fileLocation);
        workbook.write(outputStream);
        workbook.close();
        return new File(fileLocation);
    }

    private Map<String, String> getRowData(List<String> column, Product product, boolean isAdmin) throws JsonProcessingException {
        Map<String, String> rowData = new HashMap<>();
        if(isAdmin) {
            rowData.put(column.get(0), String.valueOf(product.getId()));
            rowData.put(column.get(1), product.getName());
            rowData.put(column.get(2), String.valueOf(product.getCategory()));
            rowData.put(column.get(3), categoryManager.getCategoryName(product.getCategory()));
            rowData.put(column.get(4), product.getDescription());
            rowData.put(column.get(5), product.getPrice());
            rowData.put(column.get(6), String.valueOf(resourceManager.getPhotosByProduct(product.getId()).size()));
            rowData.put(column.get(7), String.valueOf(resourceManager.getVideosByProduct(product.getId()).size()));
        }
        else {
            rowData.put(column.get(0), product.getName());
            rowData.put(column.get(1), categoryManager.getCategoryName(product.getCategory()));
            rowData.put(column.get(2), product.getDescription());
            rowData.put(column.get(3), product.getPrice());
        }

        Map<String, String> characters = Util.convertStringToMap(product.getCharacter());
        rowData.putAll(characters);
        return rowData;
    }


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
