package com.ridemotors.tgbot.telegram.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ridemotors.tgbot.constant.STATE_UPDATE_PRODUCT;
import com.ridemotors.tgbot.model.Category;
import com.ridemotors.tgbot.model.Product;
import com.ridemotors.tgbot.service.CategoryManager;
import com.ridemotors.tgbot.service.ProductManager;
import com.ridemotors.tgbot.telegram.constant.BUTTONS;
import com.ridemotors.tgbot.telegram.constant.STATE_BOT;
import com.ridemotors.tgbot.telegram.domain.AnswerBot;
import com.ridemotors.tgbot.telegram.domain.CallbackButton;
import com.ridemotors.tgbot.util.Util;
import org.checkerframework.checker.units.qual.A;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class EventAdmin extends Event {

    @Autowired
    CategoryManager categoryManager;

    public AnswerBot start(Update update) {
        List<CallbackButton> listBtn = new ArrayList<>();
        listBtn.add(new CallbackButton(BUTTONS.BTN_ADMIN_CATEGORY));
        listBtn.add(new CallbackButton(BUTTONS.BTN_ADMIN_PRODUCTS));

        return getAnswer(update, STATE_BOT.ADMIN_START, listBtn, 1);
    }

    public AnswerBot addCategory(Update update) {
        List<CallbackButton> listBtn = new ArrayList<>();
        listBtn.add(new CallbackButton(BUTTONS.BTN_BACK));
        return getAnswer(update, STATE_BOT.ADMIN_ADD_CATEGORY, listBtn, 1);
    }

    public AnswerBot deleteCategory(Update update) {
        List<CallbackButton> listBtn = new ArrayList<>();
        listBtn.add(new CallbackButton(BUTTONS.BTN_ADMIN_REMOVE_CATEGORY));
        listBtn.add(new CallbackButton(BUTTONS.BTN_BACK));
        Long category = stateDao.getCategory(update.getCallbackQuery().getMessage().getChatId());
        String name = categoryManager.getCategoryName(category);
        AnswerBot answerBot =  getAnswer(update, STATE_BOT.ADMIN_DELETE_CATEGORY, listBtn, 2);
        answerBot.setText(answerBot.getText() + name + "\nid: " + category);
        return answerBot;
    }

    public AnswerBot products(Update update) {
        List<CallbackButton> listBtn = new ArrayList<>();
        listBtn.add(new CallbackButton(BUTTONS.BTN_ADMIN_ADD_FILE_PRODUCTS));
        listBtn.add(new CallbackButton(BUTTONS.BTN_ADMIN_DOWNLOAD_ALL_PRODUCTS));
        listBtn.add(new CallbackButton(BUTTONS.BTN_ADMIN_DOWNLOAD_CATEGORY_PRODUCTS));
        listBtn.add(new CallbackButton(BUTTONS.BTN_ADMIN_ADD_FILE_RESOURCES));
        listBtn.add(new CallbackButton(BUTTONS.BTN_ADMIN_DOWNLOAD_RESOURCES));
        listBtn.add(new CallbackButton(BUTTONS.BTN_BACK));

        return getAnswer(update, STATE_BOT.ADMIN_PRODUCTS, listBtn, 1);
    }


    public AnswerBot category(Update update, Long idCategory, int numberPage) {
        List<CallbackButton> listBtn = new ArrayList<>();
        List<Category> categories = categoryManager.getChildren(idCategory);
        int pageSize = 5;
        int countPage = 1;
        if(categories.size()>0) {
            countPage = fillButtonPage(listBtn, categories, "c", pageSize, numberPage);
        }

        // В категории может быть либо другая категория либо товар, одновременно нельзя
        List<Product> products = productManager.getCategoryProducts(idCategory);
        if(categories.size()==0) {
            countPage = fillButtonPage(listBtn, products, "p", pageSize, numberPage);
        }

        listBtn.add(new CallbackButton(BUTTONS.BTN_SEPARATOR));

        if(products.size()==0)
            listBtn.add(new CallbackButton(BUTTONS.BTN_ADMIN_ADD_CATEGORY));
        listBtn.add(new CallbackButton(BUTTONS.BTN_ADMIN_DELETE_CATEGORY));
        listBtn.add(new CallbackButton(BUTTONS.BTN_BACK));
        AnswerBot answerBot = getAnswer(update, STATE_BOT.ADMIN_CATEGORY, listBtn, 1);
        if(idCategory == 0L)
            answerBot.setText(answerBot.getText() + "Корневая категория");
        else
            answerBot.setText(answerBot.getText() + categoryManager.getCategoryName(idCategory));
        answerBot.setText(answerBot.getText() + "\nИндентификатор: " + idCategory);
        addNavigateKeyboard(answerBot, "c_" + String.valueOf(idCategory), numberPage, countPage);
        stateDao.setCategory(idCategory, update.getCallbackQuery().getMessage().getChatId());
        return answerBot;
    }

    public AnswerBot loadProducts(Update update) {
        List<CallbackButton> listBtn = new ArrayList<>();
        listBtn.add(new CallbackButton(BUTTONS.BTN_BACK));
        return getAnswer(update, STATE_BOT.ADMIN_LOAD_PRODUCTS, listBtn, 1);
    }

    public AnswerBot receiveProducts(Update update) {
        String fileName = update.getMessage().getDocument().getFileName();
        String fileId = update.getMessage().getDocument().getFileId();
        String catalog= botConfig.getDirectory() + "/temp/";
        String pathFile = "";
        try {
            Util.createDirectory(catalog);
            pathFile = uploadFile(fileName, fileId, catalog);
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new RuntimeException();
        }
        File excel = new File(pathFile);
        System.out.println(excel.toPath().toAbsolutePath());
        STATE_UPDATE_PRODUCT state = productManager.updateProducts(excel);
        Util.clearDirectory(catalog);

        if(state.equals(STATE_UPDATE_PRODUCT.SUCCESS))
            return getAnswer(update, STATE_BOT.ADMIN_LOAD_SUCCESS, null, 1);
        else if(state.equals(STATE_UPDATE_PRODUCT.WARNING)) {
            AnswerBot answer = getAnswer(update, STATE_BOT.ADMIN_LOAD_SUCCESS, null, 1);
            String warning = state.getTextWarning()!=null ? state.getTextWarning() : "";
            answer.setText(answer.getText() + warning);
            List<Long> idList = state.getIdListNotFound();
            if(state.getIdListNotFound()!=null) {
                answer.setText(answer.getText() + "Не удалены следующие товары, по причине отсутствия указанного id\n" + idList.toString());
            }
            return answer;
        }
        else if(state.equals(STATE_UPDATE_PRODUCT.FAILED)){
            AnswerBot answer = getAnswer(update, STATE_BOT.ADMIN_LOAD_FAILED, null, 1);
            String failed = state.getTextFailed()!=null ? state.getTextFailed() : "";
            answer.setText(answer.getText() + failed);
            return answer;
        }
        return null;
    }

    public AnswerBot createCategory(Update update) {
        Long parentId = stateDao.getCategory(update.getMessage().getChatId());
        String name = update.getMessage().getText();
        if(name.isBlank()){
            AnswerBot answerBot = getAnswer(update, STATE_BOT.ADMIN_ADD_CATEGORY, null, 1);
            answerBot.setText(answerBot.getText() + "Имя не должно быть пустым.\nВведите снова");
            return answerBot;
        }
        categoryManager.createCategory(parentId, update.getMessage().getText());
        List<CallbackButton> listBtn = new ArrayList<>();
        listBtn.add(new CallbackButton(BUTTONS.BTN_BACK));
        AnswerBot answerBot = getAnswer(update, STATE_BOT.ADMIN_ADD_CATEGORY_SUCCESS, listBtn, 1);
        return answerBot;
    }


    public AnswerBot removeCategory(Update update) {
        Long id = stateDao.getCategory(update.getCallbackQuery().getMessage().getChatId());
        String name = categoryManager.getCategoryName(id);
        categoryManager.deleteCategory(id);
        AnswerBot answerBot = getAnswer(update, STATE_BOT.ADMIN_DELETE_CATEGORY_SUCCESS, null, 1);
        answerBot.setText(name + answerBot.getText());
        return answerBot;
    }
}
