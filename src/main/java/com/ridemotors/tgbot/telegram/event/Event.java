package com.ridemotors.tgbot.telegram.event;

import com.ridemotors.tgbot.config.BotConfig;
import com.ridemotors.tgbot.dao.StateDao;
import com.ridemotors.tgbot.telegram.TelegramBot;
import com.ridemotors.tgbot.telegram.constant.STATE_BOT;
import com.ridemotors.tgbot.telegram.domain.AnswerBot;
import com.ridemotors.tgbot.telegram.domain.CallbackButton;
import com.vdurmont.emoji.EmojiParser;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Event {

    @Autowired
    protected StateDao stateDao;

    @Autowired
    protected BotConfig botConfig;

    protected final Logger log = LoggerFactory.getLogger(TelegramBot.class);

    public AnswerBot commandNotSupport(Update update) {
        SendMessage send = new SendMessage(String.valueOf(update.getMessage().getChatId()), "Команда не поддерживается");
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

    protected void deleteFile(String catalog) {
        new File(catalog).mkdirs();
        Path path= Paths.get(catalog);
        if(Files.exists(path))
            for (File myFile : new File(catalog).listFiles())
                if (myFile.isFile()) myFile.delete();
    }
}
