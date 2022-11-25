package com.ridemotors.tgbot.telegram.constant;

public enum BUTTONS {

    BTN_SEPARATOR("--------------------------------------"),
    BTN_BACK("Назад"),
    BTN_CATEGORY("Категории"),
    BTN_PRODUCTS("Товары"),
    BTN_LOAD_EXCEL_PRODUCTS("Загрузить файл с товарами"),
    BTN_DOWNLOAD_ALL_PRODUCTS("Скачать все товары Excel"),
    BTN_ADD_FILE_RESOURCES("Загрузить фото и видео"),
    BTN_DOWNLOAD_PRODUCTS_BY_CATEGORY("Скачать товары по категории"),
    BTN_ADD_CATEGORY("Добавить категорию"),
    BTN_DELETE_CATEGORY("Удалить категорию"),
    BTN_DELETE_ALL_RESOURCE("Удалить все Фото и Видео"),
    BTN_DELETE_RESOURCE_BY_ID("Удалить Фото и Видео товара"),
    BTN_DELETE("Удалить"),
    BTN_VIDEO("Видео"),
    BTN_PHOTO("Фото");

    private String text;

    BUTTONS(String txt) {
        text = txt;
    }

    public String getText() {
        return text;
    }
}
