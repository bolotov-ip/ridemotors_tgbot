package com.ridemotors.tgbot.telegram.handler;

import com.ridemotors.tgbot.dao.StateDao;
import com.ridemotors.tgbot.telegram.TelegramBot;
import com.ridemotors.tgbot.telegram.constant.BUTTONS;
import com.ridemotors.tgbot.telegram.constant.COMMANDS;
import com.ridemotors.tgbot.telegram.constant.STATE_BOT;
import com.ridemotors.tgbot.telegram.domain.AnswerBot;
import com.ridemotors.tgbot.telegram.event.EventUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class UserHandler implements Handler{

    @Autowired
    private EventUser eventUser;

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
        if(update.hasMessage() && update.getMessage().hasSuccessfulPayment()) {

        }
        if (update.hasMessage() && update.getMessage().hasText()) {
            String textMessage = update.getMessage().getText();

            if(textMessage.equals(COMMANDS.COMMAND_START.getText())) {
                return eventUser.start(update);
            }
        }
        if(update.hasCallbackQuery()) {

            String callbackData = update.getCallbackQuery().getData();

            if(callbackData.equals(BUTTONS.BTN_DOWNLOAD_ALL_PRODUCTS.toString()))
                return eventUser.downloadExcelProduct(update, 0L);

            if(callbackData.equals(BUTTONS.BTN_BACK.toString()))
                return eventUser.back(update);

            STATE_BOT state = stateDao.getState(update.getCallbackQuery().getMessage().getChatId());
            if(state.equals(STATE_BOT.USER_START)){
                String[] data = callbackData.split("_");
                if(data.length==3) {
                    if(data[0].equals("c"))
                        return eventUser.menuCategory(update, Long.valueOf(data[1]), Integer.valueOf(data[2]));
                    if(data[0].equals("p"))
                        return eventUser.viewProduct(update, Long.valueOf(data[1]));
                }
            }
            if(state.equals(STATE_BOT.VIEW_PRODUCT)){
                String[] data = callbackData.split("_");
                if(data.length==2) {
                    if(data[0].equals("photo"))
                        return eventUser.sendImages(update, Long.valueOf(data[1]));
                    if(data[0].equals("video"))
                        return eventUser.sendVideos(update, Long.valueOf(data[1]));
                }
            }

        }
        return eventUser.commandNotSupport(update);
    }
}
