package com.ridemotors.tgbot.service;

import com.ridemotors.tgbot.constant.STATE_UPDATE_RESOURCES;
import com.ridemotors.tgbot.util.UtilFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class ResourceManager {

    @Autowired
    UtilFile utilFile;

    private final Logger log = LoggerFactory.getLogger(ResourceManager.class);

    public STATE_UPDATE_RESOURCES addResources(String pathFileZip) {
        String pathResources = utilFile.getPathResources();
        boolean isSuccess = utilFile.extractFile(pathFileZip, pathResources);
        if(isSuccess)
            return STATE_UPDATE_RESOURCES.SUCCESS;
        else
            return STATE_UPDATE_RESOURCES.FAILED;
    }

    public STATE_UPDATE_RESOURCES deleteResourceByIdProduct(Long id) {
        String pathResource = utilFile.getPathResources() + String.valueOf(id);
        utilFile.deleteDirectory(new File(pathResource));
        return STATE_UPDATE_RESOURCES.SUCCESS;
    }

    public STATE_UPDATE_RESOURCES deleteAllResources() {
        String pathResources = utilFile.getPathResources();
        utilFile.clearDirectory(pathResources);
        return STATE_UPDATE_RESOURCES.SUCCESS;
    }

    public List<String> getAllNamesResources() {
        List<String> fileNames = new ArrayList<>();
        String pathResources = utilFile.getPathResources();
        File directory = new File(pathResources);
        for(File file : directory.listFiles()) {
            fileNames.add(file.getName());
        }
        return fileNames;
    }

    public List<File> getImages(Long idProduct) {
        List<File> images = new ArrayList<>();
        String pathResource = utilFile.getPathResources() + String.valueOf(idProduct);
        List<String> formats = new ArrayList<>();
        formats.add(".png"); formats.add(".jpg"); formats.add(".jpeg"); formats.add(".gif"); formats.add(".jp2"); formats.add(".raw");
        File directory = new File(pathResource);
        for(File file : directory.listFiles()) {
            String fileName = file.getName();
            for(String format : formats) {
                if(fileName.endsWith(format)){
                    images.add(file);
                }
            }
        }
        return images;
    }

    public List<File> getVideos(Long idProduct) {
        List<File> videos = new ArrayList<>();
        String pathResource = utilFile.getPathResources() + String.valueOf(idProduct);
        List<String> formats = new ArrayList<>();
        formats.add(".mp4"); formats.add(".wmv"); formats.add(".avi"); formats.add(".m4v"); formats.add(".mov"); formats.add(".mpg");
        File directory = new File(pathResource);
        for(File file : directory.listFiles()) {
            String fileName = file.getName();
            for(String format : formats) {
                if(fileName.endsWith(format)){
                    videos.add(file);
                }
            }
        }
        return videos;
    }
}
