package com.ridemotors.tgbot.service;

import com.ridemotors.tgbot.config.BotConfig;
import com.ridemotors.tgbot.constant.ACCESS_ROLE;
import com.ridemotors.tgbot.dao.UserDao;
import com.ridemotors.tgbot.model.User;
import com.ridemotors.tgbot.telegram.TelegramBot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.List;
import java.util.Optional;

@Service
public class UserManager {

    @Autowired
    UserDao userDao;

    @Autowired
    BotConfig config;

    private final Logger log = LoggerFactory.getLogger(TelegramBot.class);

    public User findUserByUsername(String username) {
        User user = userDao.findByUserName(username);
        if(user!=null)
            return user;
        return null;
    }
    public List<User> findAllAdmin() {
        List<User> users = userDao.findByRoleContainingIgnoreCase("ADMIN");
        return users;
    }

    public User registerUser(Message msg) {
        boolean isOwner = msg.getChat().getUserName().equals(config.getOwnerName());

        User user = isOwner ? User.createUserAdmin(msg) : User.createUser(msg);
        userDao.save(user);
        log.info("user saved: " + user);
        return user;
    }

    public boolean isOwner(Long chatId) {
        User user = getUser(chatId);
        if(user == null) {
            log.error("user not exist");
            throw new RuntimeException("user not exist");
        }
        if(user.getUserName().equals(config.getOwnerName()))
            return true;
        return false;
    }

    public User getUser(Message msg) {
        Optional<User> optUser = userDao.findById(msg.getChatId());
        if(optUser.isPresent())
            return optUser.get();
        else
            return registerUser(msg);
    }

    public User getUser(Long chatId) {
        Optional<User> optUser = userDao.findById(chatId);
        if(optUser.isPresent())
            return optUser.get();
        return null;
    }

    public void removeAdminRole(Long chatId) {
        User user = getUser(chatId);
        user.setRole(ACCESS_ROLE.USER.toString());
        userDao.save(user);
    }

    public void setAdminRole(Long chatId) {
        User user = getUser(chatId);
        user.setRole(ACCESS_ROLE.USER + ";" + ACCESS_ROLE.ADMIN);
        userDao.save(user);
    }
}
