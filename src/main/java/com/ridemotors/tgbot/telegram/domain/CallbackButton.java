package com.ridemotors.tgbot.telegram.domain;

import com.ridemotors.tgbot.telegram.constant.BUTTONS;

public class CallbackButton {

    String stringButton;
    BUTTONS enumButton;
    String callbackData;

    public CallbackButton(String name) {
        stringButton = name;
    }

    public CallbackButton(BUTTONS name) {
        enumButton = name;
    }

    public String getCallbackData() {
        if(callbackData!=null)
            return callbackData;
        else if(enumButton!=null)
            return enumButton.toString();
        else
            return stringButton;
    }

    public String getText() {
        if(enumButton!=null)
            return enumButton.getText();
        else
            return stringButton;
    }

    public void setCallbackData(String callbackData) {
        this.callbackData = callbackData;
    }
}
