package com.ridemotors.tgbot.telegram.constant;

import java.util.HashMap;
import java.util.Map;

public enum STATE_BOT {
    ADMIN_START,
    ADMIN_CATEGORY,
    ADMIN_LIST_PRODUCTS,
    ADMIN_PRODUCTS,
    ADMIN_LOAD_PRODUCTS,
    ADMIN_LOAD_SUCCESS,
    ADMIN_LOAD_WARNING,
    ADMIN_LOAD_FAILED,
    ADMIN_ADD_CATEGORY,
    ADMIN_ADD_CATEGORY_SUCCESS,
    ADMIN_DELETE_CATEGORY,
    ADMIN_DELETE_CATEGORY_SUCCESS,
    ADMIN_ADD_FILES_RESOURCES,
    ADMIN_ADD_FILES_RESOURCES_SUCCESS,
    ADMIN_ADD_FILES_RESOURCES_PROCESS,
    ADMIN_ADD_FILES_RESOURCES_FAILED,
    VIEW_PRODUCT;

    private static Map<String, String> textMessage = new HashMap<String, String>();
    static {
        textMessage.put("ADMIN_START", "Добро пожаловать.\nВы являетесь администратором.");
        textMessage.put("ADMIN_CATEGORY", "Категория: ");
        textMessage.put("ADMIN_PRODUCTS", "Вы можете скачать все товары нажав соответствующую кнопку\n" +
                "Для того чтобы скачать товары которые являются дочернимим к определенной категории нажмите 'Товары по категории'");
        textMessage.put("ADMIN_LOAD_PRODUCTS", "Загрузите нужный файл с товарами");
        textMessage.put("ADMIN_LOAD_SUCCESS", "Загрузка прошла успешно");
        textMessage.put("ADMIN_LOAD_WARNING", "Загрузка прошла но возникли следующие предупреждения:\n");
        textMessage.put("ADMIN_LOAD_FAILED", "Загрузка не удалась по причине:\n");
        textMessage.put("ADMIN_LIST_PRODUCTS", "Товары в категории: ");
        textMessage.put("VIEW_PRODUCT", "Информация о товаре\n");
        textMessage.put("ADMIN_ADD_CATEGORY", "Введите наименование категории\n");
        textMessage.put("ADMIN_ADD_CATEGORY_SUCCESS", "Категория добавлена успешно\n");
        textMessage.put("ADMIN_DELETE_CATEGORY", "Вы точно хотите удалить категорию\n");
        textMessage.put("ADMIN_DELETE_CATEGORY_SUCCESS", " успешно удалена\n");
        textMessage.put("ADMIN_ADD_FILES_RESOURCES", "Загрузите файлы для товаров.\n" +
                "Файлы находятс в одной zip папке и размер этой папки не должен превышать 100Mb\n" +
                "Файлы каждого товара находятся в папке вложенной в zip папку и эта папка имеет номер id товара");
        textMessage.put("ADMIN_ADD_FILES_RESOURCES_PROCESS", "Файлы добавляются, пожалуйста подождите извещения об окончании добавления\n");
        textMessage.put("ADMIN_ADD_FILES_RESOURCES_SUCCESS", "Файлы добавлены успешно\n");
        textMessage.put("ADMIN_ADD_FILES_RESOURCES_FAILED", "Фо время добавления файлов произошла ошибка\n");
    }

    public String getTextMessage() {
        return textMessage.get(this.toString());
    }
}
