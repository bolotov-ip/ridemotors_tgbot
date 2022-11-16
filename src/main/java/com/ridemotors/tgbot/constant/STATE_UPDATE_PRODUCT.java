package com.ridemotors.tgbot.constant;

import java.util.List;

public enum STATE_UPDATE_PRODUCT {
    SUCCESS, FAILED, WARNING;

    //Warning
    List<Long> idListNotFound;
    String textWarning;

    //Failed
    String textFailed;

    public String getTextWarning() {
        return textWarning;
    }

    public void setTextWarning(String textWarning) {
        this.textWarning = textWarning;
    }

    public String getTextFailed() {
        return textFailed;
    }

    public void setTextFailed(String textFailed) {
        this.textFailed = textFailed;
    }

    public List<Long> getIdListNotFound() {
        if(this.equals(STATE_UPDATE_PRODUCT.WARNING))
            return idListNotFound;
        else
            throw  new RuntimeException("idListNotFound возможно получить только для состояния WARNING");
    }

    public void setIdListNotFound(List<Long> idNotFound) {
            this.idListNotFound = idNotFound;
    }
}
