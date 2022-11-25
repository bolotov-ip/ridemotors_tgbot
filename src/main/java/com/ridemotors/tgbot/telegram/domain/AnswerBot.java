package com.ridemotors.tgbot.telegram.domain;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.*;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;

public class AnswerBot {
    BotApiMethod<?> message;
    SendDocument document;
    SendMediaGroup mediaGroup;
    SendVideo sendVideo;
    SendPhoto sendPhoto;

    public SendVideo getSendVideo() {
        return sendVideo;
    }

    public void setSendVideo(SendVideo sendVideo) {
        this.sendVideo = sendVideo;
    }

    public SendPhoto getSendPhoto() {
        return sendPhoto;
    }

    public void setSendPhoto(SendPhoto sendPhoto) {
        this.sendPhoto = sendPhoto;
    }

    public SendMediaGroup getMediaGroup() {
        return mediaGroup;
    }

    public void setMediaGroup(SendMediaGroup mediaGroup) {
        this.mediaGroup = mediaGroup;
    }

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

    public boolean hasVideo() {
        if(sendVideo !=null)
            return true;
        return false;
    }
    public boolean hasPhoto() {
        if(sendPhoto !=null)
            return true;
        return false;
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

    public boolean hasMedia() {
        if(mediaGroup !=null)
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
