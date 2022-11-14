package com.ridemotors.tgbot.util;

import com.ridemotors.tgbot.domain.DocumentRead;
import com.ridemotors.tgbot.exception.FormatExcelException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class ExcelManager {

     private enum TYPE_ROW {
        PRODUCT, CATEGORY, HEADER, SERVICE, EMPTY;
    }

    private final Logger log = LoggerFactory.getLogger(ExcelManager.class);

    public DocumentRead parse(File file) throws IOException, FormatExcelException {
        log.info("Считывание файла {} {}", file.getName(), new Date(System.currentTimeMillis()));
        Workbook workbook = new XSSFWorkbook(new FileInputStream(file));
        Sheet sheet = workbook.getSheetAt(0);
        Row previousRow = null;
        List<String> columnNames = null;
        List<String> categories = null;
        for (Row row : sheet) {
            switch (getTypeRow(previousRow, row)){
                case EMPTY:
                case SERVICE:
                    continue;
                case HEADER:
                    columnNames = new ArrayList<>();
                    break;
                case PRODUCT:
                    break;
                case CATEGORY:
                    categories = new ArrayList<>();
                    break;
            }
            previousRow = row;
        }
        log.info("Считывание файла {} завершено успешно", file.getName());
        return null;
    }

    private TYPE_ROW getTypeRow(Row previousRow, Row row) throws FormatExcelException {
        if(previousRow!=null){
            Cell previousFirstCell = previousRow.getCell(0);
            boolean isPreviousStringCell = previousFirstCell.getCellType().equals(CellType.STRING);

            Cell currentFirstCell = row.getCell(0);
            boolean isCurrentStringCell = currentFirstCell.getCellType().equals(CellType.STRING);

            if(isPreviousStringCell && previousFirstCell.getStringCellValue().equals("Категория")) {
                if(currentFirstCell.getStringCellValue()!=null)
                    return TYPE_ROW.CATEGORY;
                else
                    throw new FormatExcelException("После ключевого слова \"Категория\", в следующей строке нужно перечислить категории");
            }
            else if(isCurrentStringCell && currentFirstCell.getStringCellValue().equals("id")) {
                return TYPE_ROW.HEADER;
            }
            if(isEmptyRow(row))
                return TYPE_ROW.EMPTY;
            else
                return TYPE_ROW.PRODUCT;
        }
        else {
            if(isEmptyRow(row))
                return TYPE_ROW.EMPTY;
            else
                return TYPE_ROW.SERVICE;
        }
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

    public File generateExcel(DocumentRead doc) {
        return null;
    }
}
