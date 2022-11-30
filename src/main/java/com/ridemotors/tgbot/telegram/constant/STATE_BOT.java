package com.ridemotors.tgbot.telegram.constant;

import java.util.HashMap;
import java.util.Map;

public enum STATE_BOT {
    VIEW_PRODUCT,
    VIEW_PRODUCT_SEARCH,

    ADMIN_START,
    ADMIN_ADD_ADMIN,
    ADMIN_LIST_ADMINS,
    ADMIN_CATEGORY,
    ADMIN_PRODUCTS,
    ADMIN_LOAD_PRODUCTS,
    ADMIN_ADD_CATEGORY,
    ADMIN_DELETE_CATEGORY,
    ADMIN_ADD_FILES_RESOURCES,
    ADMIN_DELETE_RESOURCES_BY_ID,
    ADMIN_DELETE_ALL_RESOURCES,
    ADMIN_DOWNLOAD_EXCEL_PRODUCT_BY_CATEGORY,
    SEARCH_PRODUCT,
    USER_START,
    USER_DOWNLOAD_EXCEL_PRODUCT_BY_CATEGORY,
    USER_CATEGORY;


    private static Map<String, String> textMessage = new HashMap<String, String>();
    static {
        textMessage.put("ADMIN_START", "Добро пожаловать.\nВы являетесь администратором.");
        textMessage.put("ADMIN_CATEGORY", "Категория: ");
        textMessage.put("ADMIN_PRODUCTS", "Вы можете скачать все товары нажав соответствующую кнопку\n" +
                "Для того чтобы скачать товары которые являются дочернимим к определенной категории нажмите 'Товары по категории'");
        textMessage.put("ADMIN_LOAD_PRODUCTS", "Загрузите нужный файл с товарами");
        textMessage.put("VIEW_PRODUCT", "Информация о товаре\n");
        textMessage.put("ADMIN_ADD_CATEGORY", "Введите наименование категории\n");
        textMessage.put("ADMIN_DELETE_CATEGORY", "Вы точно хотите удалить категорию\n");
        textMessage.put("ADMIN_ADD_FILES_RESOURCES", "Загрузите видео и фото для товаров.\n" +
                "Отправьте фото и видео указав в подписи (caption) идентификатор товара\n" +
                "Вы можете указать подпись только для первого фото, остальные привяжутся к этому товару автоматически\n" +
                "Когда вы начнете загружать медиа к следующему товару, снова укажите его id в подписи");
        textMessage.put("ADMIN_DELETE_ALL_RESOURCES", "Вы точно хотите удалить все фото и видео?\n");
        textMessage.put("ADMIN_DELETE_RESOURCES_BY_ID", "Введите идентификатор товара для которого удаляете видео и фото\n");
        textMessage.put("ADMIN_DOWNLOAD_EXCEL_PRODUCT_BY_CATEGORY", "Введите идентификатор категории.\n");

        textMessage.put("USER_START", "Добро пожаловать в телеграм бот RideMotors\n" +
                "Здесь представлен весь ассортимент товаров\n");
        textMessage.put("USER_CATEGORY", "Категория: ");
        textMessage.put("USER_DOWNLOAD_EXCEL_PRODUCT_BY_CATEGORY", "Введите идентификатор категории.\n");
        textMessage.put("SEARCH_PRODUCT", "Введите часть имени для поиска\n");
        textMessage.put("VIEW_PRODUCT_SEARCH", "Введите часть имени для поиска\n");
        textMessage.put("ADMIN_ADD_ADMIN", "Введите username пользователя\n");
        textMessage.put("ADMIN_LIST_ADMINS", "Список администраторов:\n");
    }

    public String getTextMessage() {
        return textMessage.get(this.toString());
    }
}
