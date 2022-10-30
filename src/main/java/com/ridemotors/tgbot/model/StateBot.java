package com.ridemotors.tgbot.model;

import com.ridemotors.tgbot.telegram.constant.STATE_BOT;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity(name = "state_bot")
public class StateBot {

    public static StateBot createStateBot(Long chatId, STATE_BOT state) {
        StateBot stateBot = new StateBot();
        stateBot.setChatId(chatId);
        stateBot.setStateBot(state.toString());
        return stateBot;
    }

    @Id
    Long chatId;

    String stateBot;

    public Long getChatId() {
        return chatId;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public String getStateBot() {
        return stateBot;
    }

    public void setStateBot(String stateBot) {
        this.stateBot = stateBot;
    }
}
