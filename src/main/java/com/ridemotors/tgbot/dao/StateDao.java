package com.ridemotors.tgbot.dao;

import com.ridemotors.tgbot.model.StateBot;
import com.ridemotors.tgbot.telegram.constant.STATE_BOT;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StateDao extends JpaRepository<StateBot, Long> {

    default void setState(STATE_BOT state, Long chatId) {

        Optional<StateBot> oldState = findById(chatId);
        if(oldState.isEmpty()) {
            StateBot newState = StateBot.createStateBot(chatId, state);
            save(newState);
        }
        else {
            oldState.get().setStateBot(state.toString());
            save(oldState.get());
        }
    }

    default STATE_BOT getState(Long chatId) {
        Optional<StateBot> state = findById(chatId);
        if(state.isPresent())
            return STATE_BOT.valueOf(state.get().getStateBot());
        else
            return STATE_BOT.ADMIN_START;
    }

    default void setCategory(Long category, Long chatId) {
        Optional<StateBot> state = findById(chatId);
        state.get().setCategory(category);
        save(state.get());
    }

    default Long getCategory(Long chatId) {
        Optional<StateBot> state = findById(chatId);
        if(state.isPresent())
            return state.get().getCategory();
        else
            return -1L;
    }

    default void setProduct(Long product, Long chatId) {
        Optional<StateBot> state = findById(chatId);
        state.get().setProduct(product);
        save(state.get());
    }

    default Long getProduct(Long chatId) {
        Optional<StateBot> state = findById(chatId);
        if(state.isPresent())
            return state.get().getProduct();
        else
            return -1L;
    }
}
