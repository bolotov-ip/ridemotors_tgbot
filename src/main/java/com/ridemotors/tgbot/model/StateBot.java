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

    Long category;

    Long product;

    public Long getProduct() {
        return product;
    }

    public void setProduct(Long product) {
        this.product = product;
    }

    public Long getCategory() {
        return category;
    }

    public void setCategory(Long category) {
        this.category = category;
    }

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
