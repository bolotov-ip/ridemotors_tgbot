package com.ridemotors.tgbot.model;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity(name = "resource")
public class Resource {
    @Id
    String fileId;

    Long productId;

    String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }
}
