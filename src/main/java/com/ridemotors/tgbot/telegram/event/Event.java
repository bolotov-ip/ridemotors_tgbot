package com.ridemotors.tgbot.telegram.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ridemotors.tgbot.config.BotConfig;
import com.ridemotors.tgbot.dao.StateDao;
import com.ridemotors.tgbot.model.Button;
import com.ridemotors.tgbot.model.Product;
import com.ridemotors.tgbot.model.Resource;
import com.ridemotors.tgbot.service.CategoryManager;
import com.ridemotors.tgbot.service.ProductManager;
import com.ridemotors.tgbot.service.UserManager;
import com.ridemotors.tgbot.telegram.TelegramBot;
import com.ridemotors.tgbot.telegram.constant.BUTTONS;
import com.ridemotors.tgbot.telegram.constant.STATE_BOT;
import com.ridemotors.tgbot.telegram.domain.AnswerBot;
import com.ridemotors.tgbot.telegram.domain.CallbackButton;
import com.ridemotors.tgbot.service.ResourceManager;
import com.ridemotors.tgbot.util.Util;
import com.vdurmont.emoji.EmojiParser;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaVideo;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Event {

    @Autowired
    protected StateDao stateDao;

    @Autowired
    protected BotConfig botConfig;

    @Autowired
    ProductManager productManager;

    @Autowired
    ResourceManager resourceManager;

    @Autowired
    CategoryManager categoryManager;

    @Autowired
    UserManager userManager;

    protected final Logger log = LoggerFactory.getLogger(TelegramBot.class);

    public AnswerBot findProducts(Update update, String inputText, int numberPage) {
        int pageSize = 5;
        Long chatId = update.hasCallbackQuery() ? update.getCallbackQuery().getMessage().getChatId() : update.getMessage().getChatId();
        stateDao.setSearchText(inputText, chatId);
        List<Product> findProducts = productManager.searchProductByName(inputText);
        List<CallbackButton> listBtn = new ArrayList<>();
        int countPage = fillButtonPage(listBtn, findProducts, "p", pageSize, numberPage);
        listBtn.add(new CallbackButton(BUTTONS.BTN_BACK));
        AnswerBot answerBot = getAnswer(update, STATE_BOT.SEARCH_PRODUCT, listBtn, 1);
        answerBot.setText("??????????????: " + findProducts.size());
        addNavigateKeyboard(answerBot, "s_" + inputText, numberPage, countPage);
        return answerBot;
    }

    public AnswerBot info(Update update, STATE_BOT state, String text, boolean isAddText) {
        List<CallbackButton> listBtn = new ArrayList<>();
        listBtn.add(new CallbackButton(BUTTONS.BTN_BACK));
        AnswerBot answerBot = getAnswer(update, state, listBtn, 1);
        if(isAddText)
            answerBot.setText(answerBot.getText() + text);
        else
            answerBot.setText(text);
        return answerBot;
    }

    public AnswerBot sendImages(Update update, Long idProduct) {
        String chatId = String.valueOf(update.getCallbackQuery().getMessage().getChatId());
        List<Resource> photos = resourceManager.getPhotosByProduct(idProduct);
        STATE_BOT stateBot = stateDao.getState(Long.valueOf(chatId));
        if(photos.size()==0) {
            AnswerBot answerBot = viewProduct(update, idProduct, stateBot);
            answerBot.setText(answerBot.getText() + "\n???????? ???? ??????????????");
            return answerBot;
        }
        List<InputMedia> medias = new ArrayList<>();
        if(photos.size()==1) {
            AnswerBot answerBot = new AnswerBot();
            SendVideo sendVideo =new SendVideo();
            sendVideo.setVideo(new InputFile(photos.get(0).getFileId()));
            sendVideo.setChatId(chatId);
            answerBot.setSendVideo(sendVideo);
            return answerBot;
        }
        for(Resource image : photos) {
            InputMediaPhoto mediaPhoto = new InputMediaPhoto();
            mediaPhoto.setMedia(image.getFileId());
            medias.add(mediaPhoto);
        }
        AnswerBot answerBot = new AnswerBot();
        SendMediaGroup mediaGroup = new SendMediaGroup();
        mediaGroup.setChatId(chatId);
        mediaGroup.setMedias(medias);
        answerBot.setMediaGroup(mediaGroup);
        return answerBot;
    }

    public AnswerBot sendVideos(Update update, Long idProduct) {
        String chatId = String.valueOf(update.getCallbackQuery().getMessage().getChatId());
        List<Resource> videos = resourceManager.getVideosByProduct(idProduct);
        STATE_BOT stateBot = stateDao.getState(Long.valueOf(chatId));
        if(videos.size()==0) {
            AnswerBot answerBot = viewProduct(update, idProduct, stateBot);
            answerBot.setText(answerBot.getText() + "\n?????????? ???? ??????????????");
            return answerBot;
        }
        List<InputMedia> medias = new ArrayList<>();
        if(videos.size()==1) {
            AnswerBot answerBot = new AnswerBot();
            SendVideo sendVideo =new SendVideo();
            sendVideo.setVideo(new InputFile(videos.get(0).getFileId()));
            sendVideo.setChatId(chatId);
            answerBot.setSendVideo(sendVideo);
            return answerBot;
        }
        for(Resource video : videos) {
            InputMediaVideo mediaVideo = new InputMediaVideo();
            mediaVideo.setMedia(video.getFileId());
            medias.add(mediaVideo);
        }
        AnswerBot answerBot = new AnswerBot();
        SendMediaGroup mediaGroup = new SendMediaGroup();
        mediaGroup.setChatId(chatId);
        mediaGroup.setMedias(medias);
        answerBot.setMediaGroup(mediaGroup);
        return answerBot;
    }

    public AnswerBot viewProduct(Update update, Long idProduct, STATE_BOT stateBot) {
        Product product = productManager.findProductById(idProduct);
        String descriptionProduct = "????????????????????????: " + product.getName() +"\n\n"+product.getDescription() + "\n\n";
        descriptionProduct+="????????: " + product.getPrice();
        Map<String,String> mapCharacters = null;
        try {
            mapCharacters = product.getCharacter().length()>0? Util.convertStringToMap(product.getCharacter()):new HashMap<>();
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
        for(Map.Entry<String, String> entry : mapCharacters.entrySet()){
            descriptionProduct+="\n" +entry.getKey() +": " + entry.getValue();
        }
        descriptionProduct+="\n";
        List<CallbackButton> listBtn = new ArrayList<>();
        CallbackButton btnPhoto = new CallbackButton(BUTTONS.BTN_PHOTO);
        btnPhoto.setCallbackData("photo_" + idProduct);
        listBtn.add(btnPhoto);
        CallbackButton btnVideo = new CallbackButton(BUTTONS.BTN_VIDEO);
        btnVideo.setCallbackData("video_" + idProduct);
        listBtn.add(btnVideo);
        listBtn.add(new CallbackButton(BUTTONS.BTN_BACK));
        AnswerBot answerBot = getAnswer(update, stateBot, listBtn, 2);
        answerBot.setText(answerBot.getText() + descriptionProduct);
        return answerBot;
    }

    public int fillButtonPage(List<CallbackButton> buttons, List<? extends Button> items, String prefix, int pageSize, int numberPage) {
        int countPage = 1;
        if(items.size()>0) {
            countPage = (int) Math.ceil(items.size()/(double)pageSize);
            int startPosition = (pageSize *(numberPage-1));
            int endPosition = Math.min(startPosition + pageSize, items.size());
            for(int i = startPosition; i<endPosition; i++) {
                CallbackButton btn = new CallbackButton(items.get(i).getName());
                btn.setCallbackData(prefix + "_" + items.get(i).getId() + "_1");
                buttons.add(btn);
            }
        }
        return countPage;
    }

    public AnswerBot addNavigateKeyboard(AnswerBot answerBot, String callback, int numberPage, int countPage) {

        ReplyKeyboard replyKeyboard = answerBot.getMessage() instanceof SendMessage ?
                ((SendMessage) answerBot.getMessage()).getReplyMarkup() :
                ((EditMessageText) answerBot.getMessage()).getReplyMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = ((InlineKeyboardMarkup) replyKeyboard).getKeyboard();
        List<InlineKeyboardButton> rowInLine = new ArrayList<>();
        if(numberPage>1) {
            InlineKeyboardButton buttonPrev = new InlineKeyboardButton();
            buttonPrev.setText("<");
            buttonPrev.setCallbackData(callback + "_" + (numberPage - 1));
            rowInLine.add(buttonPrev);
        }
        if(numberPage>1 || numberPage<countPage) {
            InlineKeyboardButton btnInfo = new InlineKeyboardButton();
            btnInfo.setText(numberPage + " - " + countPage);
            btnInfo.setCallbackData(callback + "_" + numberPage);
            rowInLine.add(btnInfo);
        }
        if(numberPage<countPage) {
            InlineKeyboardButton buttonNext = new InlineKeyboardButton();
            buttonNext.setText(">");
            buttonNext.setCallbackData(callback + "_" + (numberPage + 1));
            rowInLine.add(buttonNext);
        }
        rowsInLine.add(rowInLine);
        ((InlineKeyboardMarkup) replyKeyboard).setKeyboard(rowsInLine);
        replyMarkup((InlineKeyboardMarkup) replyKeyboard, answerBot.getMessage());
        return answerBot;
    }

    public AnswerBot commandNotSupport(Update update) {
        SendMessage send = new SendMessage(String.valueOf(update.getMessage().getChatId()), "?????????????? ???? ????????????????????????????");
        AnswerBot answer = new AnswerBot(send);
        return answer;
    }

    protected AnswerBot getAnswer(Update update, STATE_BOT stateBot, List<CallbackButton> btnList, int countColumn) {
        if(update.hasCallbackQuery())
            return setCommonCallbackAnswer(update, stateBot, btnList, countColumn);
        else
            return setCommonAnswer(update, stateBot, btnList, countColumn);
    }

    private AnswerBot setCommonCallbackAnswer(Update update, STATE_BOT stateBot, List<CallbackButton> btnList, int countColumn) {
        Message msg = update.getCallbackQuery().getMessage();
        String chatId = String.valueOf(msg.getChatId());
        long messageId = msg.getMessageId();
        EditMessageText editMessage = new EditMessageText();
        editMessage.setChatId(chatId);
        editMessage.setText(EmojiParser.parseToUnicode(stateBot.getTextMessage()));
        editMessage.setMessageId((int)messageId);
        setButton(btnList, editMessage, countColumn);
        AnswerBot answer = new AnswerBot(editMessage);
        stateDao.setState(stateBot, Long.valueOf(chatId));

        return answer;
    }

    private AnswerBot setCommonAnswer(Update update, STATE_BOT stateBot, List<CallbackButton> btnList, int countColumn) {
        AnswerBot answer = new AnswerBot();
        Message message = update.getMessage();
        String chatId = String.valueOf(message.getChatId());
        SendMessage sendMessage = new SendMessage(chatId, EmojiParser.parseToUnicode(stateBot.getTextMessage()));
        setButton(btnList, sendMessage, countColumn);
        answer.setMessage(sendMessage);
        stateDao.setState(stateBot, Long.valueOf(chatId));
        return answer;
    }

    protected void replyMarkup(InlineKeyboardMarkup markupInLine, BotApiMethod<?> msg) {
        if(msg instanceof SendMessage)
            ((SendMessage)msg).setReplyMarkup(markupInLine);
        else if(msg instanceof EditMessageText) {
            ((EditMessageText)msg).setReplyMarkup(markupInLine);
        }
    }

    protected void setButton(List<CallbackButton> btnList, BotApiMethod<?> msg, int countColumn) {
        if(btnList==null || btnList.size()==0)
            return;
        InlineKeyboardMarkup markupInLine = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> rowInLine = new ArrayList<>();
        int countAddRowBtn = 0;
        for(CallbackButton btn : btnList) {
            if(countAddRowBtn<countColumn){
                InlineKeyboardButton button = new InlineKeyboardButton();
                button.setText(btn.getText());
                button.setCallbackData(btn.getCallbackData());
                rowInLine.add(button);
                countAddRowBtn++;
            }
            else {
                rowsInLine.add(rowInLine);
                rowInLine = new ArrayList<>();
                InlineKeyboardButton button = new InlineKeyboardButton();
                button.setText(btn.getText());
                button.setCallbackData(btn.getCallbackData());
                rowInLine.add(button);
                countAddRowBtn=1;
            }
        }
        if(!rowsInLine.contains(rowInLine))
            rowsInLine.add(rowInLine);
        markupInLine.setKeyboard(rowsInLine);
        replyMarkup(markupInLine, msg);
    }

    protected String uploadFile(String fileName, String fileId, String pathFile) throws IOException {
        URL url = new URL("https://api.telegram.org/bot"+botConfig.getToken()+"/getFile?file_id="+fileId);
        BufferedReader in = new BufferedReader(new InputStreamReader( url.openStream()));
        String res = in.readLine();
        JSONObject jresult = new JSONObject(res);
        JSONObject path = jresult.getJSONObject("result");
        String file_path = path.getString("file_path");
        URL downoload = new URL("https://api.telegram.org/file/bot" + botConfig.getToken() + "/" + file_path);
        FileOutputStream fos = new FileOutputStream(pathFile + fileName);
        ReadableByteChannel rbc = Channels.newChannel(downoload.openStream());
        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        fos.close();
        rbc.close();
        log.info("File " + fileName + " download");
        return pathFile + fileName;
    }
}
