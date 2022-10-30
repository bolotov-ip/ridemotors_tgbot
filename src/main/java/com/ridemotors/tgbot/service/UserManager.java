package com.ridemotors.tgbot.service;

import com.ridemotors.tgbot.config.BotConfig;
import com.ridemotors.tgbot.dao.UserDao;
import com.ridemotors.tgbot.model.User;
import com.ridemotors.tgbot.telegram.TelegramBot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.Optional;

@Service
public class UserManager {

    @Autowired
    UserDao userDao;

    @Autowired
    BotConfig config;

    private final Logger log = LoggerFactory.getLogger(TelegramBot.class);

    public User registerUser(Message msg) {
        boolean isOwner = String.valueOf(msg.getChatId()).equals(config.getOwnerId());
        User user = isOwner?User.createUserAdmin(msg):User.createUser(msg);
        userDao.save(user);
        log.info("user saved: " + user);
        return user;
    }

    public boolean hasUser(Message msg) {
        Optional<User> optUser = userDao.findById(msg.getChatId());
        if(optUser.isPresent()) {
            return true;
        }
        return false;
    }

    public User getUser(Message msg) {
        Optional<User> optUser = userDao.findById(msg.getChatId());
        if(optUser.isPresent())
            return optUser.get();
        return null;
    }

    public User getUser(Long chatId) {
        Optional<User> optUser = userDao.findById(chatId);
        if(optUser.isPresent())
            return optUser.get();
        return null;
    }

    public void saveUser(User user) {
        userDao.save(user);
    }
}
