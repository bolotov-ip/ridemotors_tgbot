package com.ridemotors.tgbot.telegram;

import com.ridemotors.tgbot.service.UserManager;
import com.ridemotors.tgbot.config.BotConfig;
import com.ridemotors.tgbot.telegram.domain.AnswerBot;
import com.ridemotors.tgbot.model.User;
import com.ridemotors.tgbot.telegram.handler.AdminHandler;
import com.ridemotors.tgbot.telegram.handler.Handler;
import com.ridemotors.tgbot.telegram.handler.UserHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerPreCheckoutQuery;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

@Component
public class TelegramBot extends TelegramLongPollingBot {

    private final Logger log = LoggerFactory.getLogger(TelegramBot.class);

    @Autowired
    private AdminHandler adminHandler;

    @Autowired
    private UserHandler userHandler;

    @Autowired
    private UserManager userManager;


    @Autowired
    BotConfig config;

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {

        if(update.hasPreCheckoutQuery()) {
            AnswerPreCheckoutQuery answerPreCheckoutQuery = new AnswerPreCheckoutQuery(update.getPreCheckoutQuery().getId(), true);
            try {
                execute(answerPreCheckoutQuery);
            } catch (TelegramApiException e) {
                log.error(e.getMessage());
                throw new RuntimeException(e);
            }
        }

        Message msg = update.hasMessage()?update.getMessage():update.hasCallbackQuery()?update.getCallbackQuery().getMessage():null;
        if(msg == null)
            return;
        adminHandler.setBot(this);
        adminHandler.setUpdate(update);
        userHandler.setBot(this);
        userHandler.setUpdate(update);

        User user = userManager.getUser(msg);
        if(user==null) {
            user = userManager.registerUser(msg);
        }
        Handler handler = user.isAdmin()?adminHandler:userHandler;
        AnswerBot answer = handler.run();
        List<BotCommand> listofCommands = new ArrayList<>();
        listofCommands.add(new BotCommand("/start", "Стартовое меню"));
        try {
            execute(new SetMyCommands(listofCommands, new BotCommandScopeDefault(), null));
            if(answer.hasMessage())
                execute(answer.getMessage());
            else if(answer.hasDocument())
                execute(answer.getDocument());
            else if (answer.hasMedia()) {
                execute(answer.getMediaGroup());
            }
            else if (answer.hasPhoto()) {
                execute(answer.getSendPhoto());
            }
            else if (answer.hasVideo()) {
                execute(answer.getSendVideo());
            }
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
    }
}
