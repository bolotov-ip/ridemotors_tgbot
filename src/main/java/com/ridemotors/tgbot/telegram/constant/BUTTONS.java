package com.ridemotors.tgbot.telegram.constant;

public enum BUTTONS {

    BTN_ADMIN_RUN("Выполнить заказ");

    private String text;

    BUTTONS(String txt) {
        text = txt;
    }

    public String getText() {
        return text;
    }
}
