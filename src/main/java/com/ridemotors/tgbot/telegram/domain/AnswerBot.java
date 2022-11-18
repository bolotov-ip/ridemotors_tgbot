package com.ridemotors.tgbot.telegram.domain;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;

public class AnswerBot {
    BotApiMethod<?> message;
    SendDocument document;

    public SendDocument getDocument() {
        return document;
    }

    public void setDocument(SendDocument document) {
        this.document = document;
    }

    public AnswerBot() {
    }

    public AnswerBot(BotApiMethod<?> answer) {
        message = answer;
    }

    public BotApiMethod<?> getMessage() {
        return message;
    }

    public void setMessage(BotApiMethod<?> answer) {
        this.message = answer;
        if(message != null) {
            if(message instanceof EditMessageText){
                ((EditMessageText)message).enableHtml(true);
            }
            else if(message instanceof SendMessage) {
                ((SendMessage)message).enableHtml(true);
            }
        }
    }

    public boolean hasMessage() {
        if(message !=null)
            return true;
        return false;
    }

    public boolean hasDocument() {
        if(document !=null)
            return true;
        return false;
    }

    public void setText(String text) {
        if(message!=null){
            if(message instanceof EditMessageText){
                ((EditMessageText)message).setText(text);
            }
            else if(message instanceof SendMessage) {
                ((SendMessage)message).setText(text);
            }
        }
    }

    public String getText() {
        if(message!=null){
            if(message instanceof EditMessageText){
                return ((EditMessageText)message).getText();
            }
            else if(message instanceof SendMessage) {
                return((SendMessage)message).getText();
            }
        }
        return "";
    }
}
