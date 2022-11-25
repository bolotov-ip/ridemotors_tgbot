package com.ridemotors.tgbot.telegram.event;

import com.ridemotors.tgbot.constant.RESOURCE_TYPES;
import com.ridemotors.tgbot.constant.STATE_UPDATE_PRODUCT;
import com.ridemotors.tgbot.model.Category;
import com.ridemotors.tgbot.model.Product;
import com.ridemotors.tgbot.model.Resource;
import com.ridemotors.tgbot.service.CategoryManager;
import com.ridemotors.tgbot.telegram.constant.BUTTONS;
import com.ridemotors.tgbot.telegram.constant.STATE_BOT;
import com.ridemotors.tgbot.telegram.domain.AnswerBot;
import com.ridemotors.tgbot.telegram.domain.CallbackButton;
import com.ridemotors.tgbot.util.UtilFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.objects.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class EventAdmin extends Event {

    @Autowired
    CategoryManager categoryManager;

    @Autowired
    UtilFile utilFile;

    public AnswerBot start(Update update) {
        List<CallbackButton> listBtn = new ArrayList<>();
        listBtn.add(new CallbackButton(BUTTONS.BTN_CATEGORY));
        listBtn.add(new CallbackButton(BUTTONS.BTN_PRODUCTS));

        return getAnswer(update, STATE_BOT.ADMIN_START, listBtn, 1);
    }

    public AnswerBot menuProduct(Update update) {
        List<CallbackButton> listBtn = new ArrayList<>();
        listBtn.add(new CallbackButton(BUTTONS.BTN_LOAD_EXCEL_PRODUCTS));
        listBtn.add(new CallbackButton(BUTTONS.BTN_DOWNLOAD_ALL_PRODUCTS));
        listBtn.add(new CallbackButton(BUTTONS.BTN_DOWNLOAD_PRODUCTS_BY_CATEGORY));
        listBtn.add(new CallbackButton(BUTTONS.BTN_ADD_FILE_RESOURCES));
        listBtn.add(new CallbackButton(BUTTONS.BTN_DELETE_RESOURCE_BY_ID));
        listBtn.add(new CallbackButton(BUTTONS.BTN_DELETE_ALL_RESOURCE));
        listBtn.add(new CallbackButton(BUTTONS.BTN_BACK));

        return getAnswer(update, STATE_BOT.ADMIN_PRODUCTS, listBtn, 1);
    }


    public AnswerBot menuCategory(Update update, Long idCategory, int numberPage) {
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
            listBtn.add(new CallbackButton(BUTTONS.BTN_ADD_CATEGORY));
        listBtn.add(new CallbackButton(BUTTONS.BTN_DELETE_CATEGORY));
        listBtn.add(new CallbackButton(BUTTONS.BTN_BACK));
        AnswerBot answerBot = getAnswer(update, STATE_BOT.ADMIN_CATEGORY, listBtn, 1);
        if(idCategory == 0L)
            answerBot.setText(answerBot.getText() + "Корневая категория");
        else
            answerBot.setText(answerBot.getText() + categoryManager.getCategoryName(idCategory));
        answerBot.setText(answerBot.getText() + "\nИндентификатор: " + idCategory);
        addNavigateKeyboard(answerBot, "c_" + idCategory, numberPage, countPage);
        stateDao.setCategory(idCategory, update.getCallbackQuery().getMessage().getChatId());
        return answerBot;
    }

    public AnswerBot downloadExcelProduct(Update update, Long categoryId) {
        File excel = productManager.getExcelProducts(categoryId, true);
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

    public AnswerBot receiveResource(Update update) {
        Message message = update.getMessage();
        String textAnswer = "";
        Long productId;
        if(message.getCaption()!=null) {
            productId = Long.valueOf(message.getCaption());
            stateDao.setProduct(productId, message.getChatId());
        }
        else
            productId = stateDao.getProduct(message.getChatId());
        Product product = productManager.findProductById(productId);
        if(productId==-1L || product==null) {
            textAnswer = "Товар с таким идентификатором не существует";
            return info(update, STATE_BOT.ADMIN_ADD_FILES_RESOURCES, textAnswer, false);
        }

        if(message.hasPhoto()) {
            List<PhotoSize> photoSizes = message.getPhoto();
            if(photoSizes.size()>0) {
                PhotoSize photo = photoSizes.get(photoSizes.size()-1);
                resourceManager.addResource(productId, photo.getFileId(), RESOURCE_TYPES.PHOTO);
            }
            List<Resource> photos = resourceManager.getPhotosByProduct(productId);
            textAnswer = "Добавлено фото к товару\nНаименование: " + product.getName() +"\nid: " + productId + "\nВсего фото товара: " + photos.size();
        }

        if(message.hasVideo()) {
            Video video = message.getVideo();
            resourceManager.addResource(productId, video.getFileId(), RESOURCE_TYPES.VIDEO);
            List<Resource> videos = resourceManager.getVideosByProduct(productId);
            textAnswer = "Добавлено видео к товару\nНаименование: " + product.getName() +"\nid: " + productId + "\nВсего видео товара: " + videos.size();
        }
        return info(update, STATE_BOT.ADMIN_ADD_FILES_RESOURCES, textAnswer, false);
    }

    public AnswerBot receiveProduct(Update update) {
        String fileName = update.getMessage().getDocument().getFileName();
        String fileId = update.getMessage().getDocument().getFileId();
        String catalog= utilFile.getPathTemp();
        String pathFile = "";
        try {
            pathFile = uploadFile(fileName, fileId, catalog);
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new RuntimeException();
        }
        File excel = new File(pathFile);
        STATE_UPDATE_PRODUCT state = productManager.updateProducts(excel);
        utilFile.clearDirectory(catalog);

        if(state.equals(STATE_UPDATE_PRODUCT.SUCCESS))
            return info(update, STATE_BOT.ADMIN_LOAD_PRODUCTS, "Товары успешно обновлены согласно файлу", false);
        else if(state.equals(STATE_UPDATE_PRODUCT.WARNING)) {
            String textAnswer = state.getTextWarning()!=null ? state.getTextWarning() : "";
            List<Long> idList = state.getIdListNotFound();
            if(state.getIdListNotFound()!=null) {
                textAnswer += "Не удалены следующие товары, по причине отсутствия указанного id\n" + idList.toString();
            }
            return info(update, STATE_BOT.ADMIN_LOAD_PRODUCTS, textAnswer, false);
        }
        else if(state.equals(STATE_UPDATE_PRODUCT.FAILED)){
            String failed = state.getTextFailed()!=null ? state.getTextFailed() : "";
            return info(update, STATE_BOT.ADMIN_LOAD_PRODUCTS, failed, false);
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
        return info(update, STATE_BOT.ADMIN_ADD_CATEGORY, "Категория " + name + " успешно создана", false);
    }
    public AnswerBot confirmDelete(Update update, STATE_BOT state) {
        List<CallbackButton> listBtn = new ArrayList<>();
        listBtn.add(new CallbackButton(BUTTONS.BTN_DELETE));
        listBtn.add(new CallbackButton(BUTTONS.BTN_BACK));
        AnswerBot answerBot = getAnswer(update, state, listBtn, 1);
        return answerBot;
    }

    public AnswerBot deleteCategory(Update update) {
        Long id = stateDao.getCategory(update.getCallbackQuery().getMessage().getChatId());
        String name = categoryManager.getCategoryName(id);
        categoryManager.deleteCategory(id, productManager);
        return info(update, STATE_BOT.ADMIN_DELETE_CATEGORY, "Категория " + name + " удалена", false);
    }

    public AnswerBot deleteResourceById(Update update, Long productId) {
        Product product = productManager.findProductById(productId);
        if(product==null){
            return info(update, STATE_BOT.ADMIN_DELETE_ALL_RESOURCES, "Товара с id: " + productId +" не существует", false);
        }
        resourceManager.deleteResourceByIdProduct(productId);
        return info(update, STATE_BOT.ADMIN_DELETE_ALL_RESOURCES, "Все фото и видео товара " + productId + " удалены", false);
    }

    public AnswerBot deleteAllResources(Update update) {
        resourceManager.deleteAllResources();
        return info(update, STATE_BOT.ADMIN_DELETE_ALL_RESOURCES, "Все фото и видео товаров удалены", false);
    }

    public AnswerBot back(Update update) {
        Long chatId = Long.valueOf(update.getCallbackQuery().getMessage().getChatId());
        STATE_BOT stateBot = stateDao.getState(chatId);
        if(stateBot.equals(STATE_BOT.ADMIN_PRODUCTS))
            return start(update);
        if(stateBot.equals(STATE_BOT.ADMIN_CATEGORY)){
            Long categoryId = stateDao.getCategory(chatId);
            if(categoryId==0L)
                return start(update);
            Long parentCategoryId = categoryManager.getCategory(categoryId).getParent();
            return menuCategory(update, parentCategoryId, 1);
        }
        if(stateBot.equals(STATE_BOT.ADMIN_DELETE_CATEGORY) || stateBot.equals(STATE_BOT.ADMIN_ADD_CATEGORY) || stateBot.equals(STATE_BOT.VIEW_PRODUCT)) {
            Long categoryId = stateDao.getCategory(chatId);
            return menuCategory(update, categoryId, 1);
        }
        if(stateBot.equals(STATE_BOT.ADMIN_DELETE_RESOURCES_BY_ID) || stateBot.equals(STATE_BOT.ADMIN_ADD_FILES_RESOURCES) ||
                stateBot.equals(STATE_BOT.ADMIN_LOAD_PRODUCTS) || stateBot.equals(STATE_BOT.ADMIN_DELETE_ALL_RESOURCES) ||
                stateBot.equals(STATE_BOT.ADMIN_DOWNLOAD_EXCEL_PRODUCT_BY_CATEGORY))
            return menuProduct(update);
        return start(update);
    }
}
