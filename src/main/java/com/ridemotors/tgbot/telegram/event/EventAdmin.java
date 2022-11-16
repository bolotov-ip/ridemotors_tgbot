package com.ridemotors.tgbot.telegram.event;

import com.ridemotors.tgbot.constant.STATE_UPDATE_PRODUCT;
import com.ridemotors.tgbot.service.ProductManager;
import com.ridemotors.tgbot.telegram.constant.BUTTONS;
import com.ridemotors.tgbot.telegram.constant.STATE_BOT;
import com.ridemotors.tgbot.telegram.domain.AnswerBot;
import com.ridemotors.tgbot.telegram.domain.CallbackButton;
import com.ridemotors.tgbot.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class EventAdmin extends Event {

    private final Logger log = LoggerFactory.getLogger(EventAdmin.class);

    @Autowired
    ProductManager productManager;

    public AnswerBot start(Update update) {
        List<CallbackButton> listBtn = new ArrayList<>();
        listBtn.add(new CallbackButton(BUTTONS.BTN_ADMIN_CATEGORY));
        listBtn.add(new CallbackButton(BUTTONS.BTN_ADMIN_PRODUCTS));

        return getAnswer(update, STATE_BOT.ADMIN_START, listBtn, 1);
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
}
