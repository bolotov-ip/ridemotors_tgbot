package com.ridemotors.tgbot.telegram.handler;

import com.ridemotors.tgbot.dao.StateDao;
import com.ridemotors.tgbot.telegram.TelegramBot;
import com.ridemotors.tgbot.telegram.constant.BUTTONS;
import com.ridemotors.tgbot.telegram.constant.COMMANDS;
import com.ridemotors.tgbot.telegram.constant.STATE_BOT;
import com.ridemotors.tgbot.telegram.domain.AnswerBot;
import com.ridemotors.tgbot.telegram.event.EventAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class AdminHandler implements Handler{

    @Autowired
    private EventAdmin eventAdmin;

    @Autowired
    private StateDao stateDao;

    private final Logger log = LoggerFactory.getLogger(TelegramBot.class);

    private TelegramBot bot;

    private Update update;

    public void setBot(TelegramBot bot) {
        this.bot = bot;
    }

    public void setUpdate(Update update) {
        this.update = update;
    }

    @Override
    public AnswerBot run() {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String textMessage = update.getMessage().getText();

            if(textMessage.equals(COMMANDS.COMMAND_START.getText())) {
                return eventAdmin.start(update);
            }
            STATE_BOT state = stateDao.getState(update.getMessage().getChatId());
            if(state.equals(STATE_BOT.ADMIN_ADD_CATEGORY)) {
                return eventAdmin.createCategory(update);
            }
            if(state.equals(STATE_BOT.ADMIN_DELETE_RESOURCES_BY_ID)) {
                Long productId = -1L;
                try {
                    productId = Long.valueOf(textMessage);
                }
                catch (Exception e) {
                    log.error(e.getMessage());
                }
                if(productId == -1L)
                    return eventAdmin.commandNotSupport(update);
                return eventAdmin.deleteResourceById(update, productId);
            }
            if (state.equals(STATE_BOT.ADMIN_DOWNLOAD_EXCEL_PRODUCT_BY_CATEGORY)) {
                return eventAdmin.downloadExcelProductByCategory(update);
            }
            return eventAdmin.commandNotSupport(update);
        }
        if(update.hasCallbackQuery()) {

            String callbackData = update.getCallbackQuery().getData();

            if(callbackData.equals(BUTTONS.BTN_BACK.toString())) {
                return eventAdmin.back(update);
            }
            if(callbackData.equals(BUTTONS.BTN_PRODUCTS.toString())) {
                return eventAdmin.menuProduct(update);
            }
            if(callbackData.equals(BUTTONS.BTN_LOAD_EXCEL_PRODUCTS.toString())) {
                return eventAdmin.info(update, STATE_BOT.ADMIN_LOAD_PRODUCTS, "", true);
            }
            if(callbackData.equals(BUTTONS.BTN_CATEGORY.toString())) {
                return eventAdmin.menuCategory(update, 0L, 1);
            }
            if(callbackData.equals(BUTTONS.BTN_ADD_CATEGORY.toString())) {
                return eventAdmin.info(update, STATE_BOT.ADMIN_ADD_CATEGORY, "", true);
            }
            if(callbackData.equals(BUTTONS.BTN_DELETE_CATEGORY.toString())) {
                return eventAdmin.confirmDelete(update, STATE_BOT.ADMIN_DELETE_CATEGORY);
            }
            if(callbackData.equals(BUTTONS.BTN_ADD_FILE_RESOURCES.toString())) {
                return eventAdmin.info(update, STATE_BOT.ADMIN_ADD_FILES_RESOURCES, "", true);
            }
            if(callbackData.equals(BUTTONS.BTN_DELETE_RESOURCE_BY_ID.toString())) {
                return eventAdmin.info(update, STATE_BOT.ADMIN_DELETE_RESOURCES_BY_ID, "", true);
            }
            if(callbackData.equals(BUTTONS.BTN_DELETE_ALL_RESOURCE.toString())) {
                return eventAdmin.confirmDelete(update, STATE_BOT.ADMIN_DELETE_ALL_RESOURCES);
            }
            if(callbackData.equals(BUTTONS.BTN_DOWNLOAD_ALL_PRODUCTS.toString())) {
                return eventAdmin.downloadExcelProduct(update, 0L);
            }
            if(callbackData.equals(BUTTONS.BTN_DOWNLOAD_PRODUCTS_BY_CATEGORY.toString())) {
                return eventAdmin.info(update, STATE_BOT.ADMIN_DOWNLOAD_EXCEL_PRODUCT_BY_CATEGORY, "", true);
            }
            if(callbackData.equals(BUTTONS.BTN_DELETE.toString())) {
                STATE_BOT state = stateDao.getState(update.getCallbackQuery().getMessage().getChatId());
                if(state.equals(STATE_BOT.ADMIN_DELETE_CATEGORY))
                    return eventAdmin.deleteCategory(update);
                if(state.equals(STATE_BOT.ADMIN_DELETE_ALL_RESOURCES))
                    return eventAdmin.deleteAllResources(update);
            }

            STATE_BOT state = stateDao.getState(update.getCallbackQuery().getMessage().getChatId());
            if(state.equals(STATE_BOT.ADMIN_CATEGORY)){
                String[] data = callbackData.split("_");
                if(data.length==3) {
                    if(data[0].equals("c"))
                        return eventAdmin.menuCategory(update, Long.valueOf(data[1]), Integer.valueOf(data[2]));
                    if(data[0].equals("p"))
                        return eventAdmin.viewProduct(update, Long.valueOf(data[1]));
                }
            }
            if(state.equals(STATE_BOT.VIEW_PRODUCT)){
                String[] data = callbackData.split("_");
                if(data.length==2) {
                    if(data[0].equals("photo"))
                        return eventAdmin.sendImages(update, Long.valueOf(data[1]));
                    if(data[0].equals("video"))
                        return eventAdmin.sendVideos(update, Long.valueOf(data[1]));
                }
            }
        }

        if(update.getMessage().hasDocument()) {
            STATE_BOT state = stateDao.getState(update.getMessage().getChatId());
            if(state.equals(STATE_BOT.ADMIN_LOAD_PRODUCTS))
                return eventAdmin.receiveProduct(update);
        }

        if(update.getMessage().hasPhoto() || update.getMessage().hasVideo()) {
            STATE_BOT state = stateDao.getState(update.getMessage().getChatId());
            if(state.equals(STATE_BOT.ADMIN_ADD_FILES_RESOURCES)){
                return eventAdmin.receiveResource(update);
            }
        }

        return eventAdmin.commandNotSupport(update);
    }
}
