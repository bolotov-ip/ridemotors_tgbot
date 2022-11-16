package com.ridemotors.tgbot.telegram.constant;

public enum BUTTONS {

    BTN_BACK("Назад"),
    BTN_ADMIN_CATEGORY("Категории"),
    BTN_ADMIN_PRODUCTS("Товары"),
    BTN_ADMIN_ADD_FILE_PRODUCTS("Загрузить файл с товарами"),
    BTN_ADMIN_DOWNLOAD_ALL_PRODUCTS("Скачать все товары"),
    BTN_ADMIN_ADD_FILE_RESOURCES("Загрузить архив image, video"),
    BTN_ADMIN_DOWNLOAD_CATEGORY_PRODUCTS("Скачать товары по категории"),
    BTN_ADMIN_DOWNLOAD_RESOURCES("Скачать файлы image, video");

    private String text;

    BUTTONS(String txt) {
        text = txt;
    }

    public String getText() {
        return text;
    }
}
