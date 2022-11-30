package com.ridemotors.tgbot.telegram.event;


import com.ridemotors.tgbot.model.Category;
import com.ridemotors.tgbot.model.Product;
import com.ridemotors.tgbot.service.CategoryManager;
import com.ridemotors.tgbot.telegram.constant.BUTTONS;
import com.ridemotors.tgbot.telegram.constant.STATE_BOT;
import com.ridemotors.tgbot.telegram.domain.AnswerBot;
import com.ridemotors.tgbot.telegram.domain.CallbackButton;
import com.ridemotors.tgbot.util.UtilFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Component
public class EventUser extends Event {

    @Autowired
    CategoryManager categoryManager;

    @Autowired
    UtilFile utilFile;

    public AnswerBot start(Update update) {
        return menuCategory(update, 0L, 1);
    }

    public AnswerBot menuCategory(Update update, Long idCategory, int numberPage) {
        List<CallbackButton> listBtn = new ArrayList<>();
        List<Category> categories = categoryManager.getChildren(idCategory);
        int pageSize = idCategory==0L ? 20 : 5;
        int countPage = 1;
        if(categories.size()>0) {
            countPage = fillButtonPage(listBtn, categories, "c", pageSize, numberPage);
        }
        // В категории может быть либо другая категория либо товар, одновременно нельзя
        List<Product> products = productManager.getCategoryProducts(idCategory);
        if(categories.size()==0) {
            countPage = fillButtonPage(listBtn, products, "p", pageSize, numberPage);
        }

        if(idCategory == 0L)
            listBtn.add(new CallbackButton(BUTTONS.BTN_DOWNLOAD_ALL_PRODUCTS));
        listBtn.add(new CallbackButton(BUTTONS.BTN_SEARCH));
        if(idCategory != 0L)
            listBtn.add(new CallbackButton(BUTTONS.BTN_BACK));

        AnswerBot answerBot = getAnswer(update, STATE_BOT.USER_START, listBtn, 1);
        if(idCategory != 0L)
            answerBot.setText(answerBot.getText() + categoryManager.getCategoryName(idCategory));
        addNavigateKeyboard(answerBot, "c_" + idCategory, numberPage, countPage);
        Long chatId = update.hasCallbackQuery() ? update.getCallbackQuery().getMessage().getChatId() : update.getMessage().getChatId();
        stateDao.setCategory(idCategory, chatId);
        return answerBot;
    }

    public AnswerBot back(Update update) {
        Long chatId = Long.valueOf(update.getCallbackQuery().getMessage().getChatId());
        STATE_BOT stateBot = stateDao.getState(chatId);
        if(stateBot.equals(STATE_BOT.USER_START)){
            Long categoryId = stateDao.getCategory(chatId);
            if(categoryId==0L)
                return start(update);
            Long parentCategoryId = categoryManager.getCategory(categoryId).getParent();
            return menuCategory(update, parentCategoryId, 1);
        }
        if(stateBot.equals(STATE_BOT.VIEW_PRODUCT)) {
            Long categoryId = stateDao.getCategory(chatId);
            return menuCategory(update, categoryId, 1);
        }
        if(stateBot.equals(STATE_BOT.VIEW_PRODUCT_SEARCH)) {
            String searchText = stateDao.getSearchText(chatId);
            return findProducts(update, searchText, 1);
        }
        return start(update);
    }

    public AnswerBot downloadExcelProduct(Update update, Long categoryId) {
        File excel = productManager.getExcelProducts(categoryId, false);
        utilFile.clearDirectory(utilFile.getPathTemp());
        SendDocument sendDocument = new SendDocument();
        if(update.hasCallbackQuery())
            sendDocument.setChatId(String.valueOf(update.getCallbackQuery().getMessage().getChatId()));
        if(update.hasMessage())
            sendDocument.setChatId(String.valueOf(update.getMessage().getChatId()));
        sendDocument.setDocument(new InputFile(excel));
        AnswerBot answerBot = new AnswerBot();
        answerBot.setDocument(sendDocument);
        return answerBot;
    }

    public AnswerBot downloadExcelProductByCategory(Update update) {
        AnswerBot answerBot = getAnswer(update, STATE_BOT.ADMIN_DOWNLOAD_EXCEL_PRODUCT_BY_CATEGORY, null, 1);
        try {
            Long categoryId = Long.valueOf(update.getMessage().getText());
            if(categoryManager.existCategory(categoryId)) {
                return downloadExcelProduct(update, categoryId);
            }
            answerBot.setText("Категория не найдена");
        }
        catch (Exception e) {
            log.error(e.getMessage());
        }
        answerBot.setText("Введите числовой идентификатор");
        return null;
    }
}
