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
            return eventAdmin.commandNotSupport(update);
        } else if(update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();

            if(callbackData.equals(BUTTONS.BTN_ADMIN_PRODUCTS.toString())) {
                return eventAdmin.products(update);
            }
            else if(callbackData.equals(BUTTONS.BTN_ADMIN_ADD_FILE_PRODUCTS.toString())) {
                return eventAdmin.addProducts(update);
            }
            else if(callbackData.equals(BUTTONS.BTN_ADMIN_CATEGORY.toString())) {
                return eventAdmin.category(update, 0L, 1);
            }
            else if(callbackData.equals(BUTTONS.BTN_ADMIN_ADD_CATEGORY.toString())) {
                return eventAdmin.inputNameCategory(update);
            }
            else if(callbackData.equals(BUTTONS.BTN_ADMIN_DELETE_CATEGORY.toString())) {
                return eventAdmin.confirmDeleteCategory(update);
            }
            else if(callbackData.equals(BUTTONS.BTN_ADMIN_REMOVE_CATEGORY.toString())) {
                return eventAdmin.deleteCategory(update);
            }
            else if(callbackData.equals(BUTTONS.BTN_ADMIN_ADD_FILE_RESOURCES.toString())) {
                return eventAdmin.addResourceFiles(update);
            }

            STATE_BOT state = stateDao.getState(update.getCallbackQuery().getMessage().getChatId());
            if(state.equals(STATE_BOT.ADMIN_CATEGORY)){
                String[] data = callbackData.split("_");
                if(data.length==3) {
                    if(data[0].equals("c"))
                        return eventAdmin.category(update, Long.valueOf(data[1]), Integer.valueOf(data[2]));
                    if(data[0].equals("p"))
                        return eventAdmin.viewProduct(update, Long.valueOf(data[1]));
                }
            }
        }
        else if(update.getMessage().hasDocument()) {
            STATE_BOT state = stateDao.getState(update.getMessage().getChatId());
            if(state.equals(STATE_BOT.ADMIN_LOAD_PRODUCTS))
                return eventAdmin.downloadProductFile(update);
            if(state.equals(STATE_BOT.ADMIN_ADD_FILES_RESOURCES))
                return eventAdmin.downloadResourceFiles(update, bot);
        }
        return eventAdmin.commandNotSupport(update);
    }
}
