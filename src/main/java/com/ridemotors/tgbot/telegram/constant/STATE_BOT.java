package com.ridemotors.tgbot.telegram.constant;

import java.util.HashMap;
import java.util.Map;

public enum STATE_BOT {
    ADMIN_START;

    private static Map<String, String> textMessage = new HashMap<String, String>();
    static {
        textMessage.put("ADMIN_START", "Добро пожаловать.\nВы являетесь администратором.");
    }

    public String getTextMessage() {
        return textMessage.get(this.toString());
    }
}
