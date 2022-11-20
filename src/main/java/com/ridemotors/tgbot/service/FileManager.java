package com.ridemotors.tgbot.service;

import com.ridemotors.tgbot.config.BotConfig;
import com.ridemotors.tgbot.constant.STATE_UPDATE_RESOURCES;
import com.ridemotors.tgbot.telegram.TelegramBot;
import com.ridemotors.tgbot.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Service
public class FileManager {

    private final Logger log = LoggerFactory.getLogger(FileManager.class);
    @Autowired
    BotConfig botConfig;

    public String getPathResources() {
        String path = getPathRootApp() +"resources" +File.separator;
        createDirectory(path);
        return path;
    }

    public String getPathTemp() {
        String path = getPathRootApp() + "temp" + File.separator;
        createDirectory(path);
        return path;
    }

    public String getPathRootApp() {
        String pathBotConfig = botConfig.getDirectory().replaceAll("_", File.separator);
        File[] root = File.listRoots();
        String path = root[0].toPath()+File.separator+pathBotConfig+File.separator;
        createDirectory(path);
        return path;
    }

    public void createDirectory(String path) {
        var directory = new File(path);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    public STATE_UPDATE_RESOURCES extractFile(String pathFile) {
        STATE_UPDATE_RESOURCES answer = STATE_UPDATE_RESOURCES.SUCCESS;
        try (var file = new ZipFile(pathFile)) {
            var entries = file.entries();
            var uncompressedDirectory = getPathResources();
            while (entries.hasMoreElements()) {
                var entry = entries.nextElement();
                if (entry.isDirectory()) {
                    processDirectory(uncompressedDirectory, entry);
                } else {
                    processFile(file, uncompressedDirectory, entry);
                }
            }
        } catch (IOException e) {
            log.error(e.getMessage());
            answer = STATE_UPDATE_RESOURCES.FAILED;
        }
        return answer;
    }

    private void processDirectory(String uncompressedDirectory, ZipEntry entry) {
        var newDirectory = uncompressedDirectory + entry.getName();
        var directory = new File(newDirectory);
        if(directory.exists())
            Util.recursiveDelete(directory);
        log.info("Creating Directory: {}", newDirectory);
        createDirectory(newDirectory);
    }

    private void processFile(ZipFile file, String uncompressedRootDirectory, ZipEntry entry) throws IOException {
        try (
                var is = file.getInputStream(entry);
                var bis = new BufferedInputStream(is)
        ) {
            var uncompressedFileName = uncompressedRootDirectory + entry.getName();
            createDirectory(getUncompressedDirectory(uncompressedRootDirectory, entry.getName()));
            try (
                var os = new FileOutputStream(uncompressedFileName);
                var bos = new BufferedOutputStream(os)
            ) {
                while (bis.available() > 0) {
                    bos.write(bis.read());
                }
            }
        }
        log.info("Written: {}", entry.getName());
    }

    private String getUncompressedDirectory(String uncompressedDirectory, String name) {
        if(name.contains(".")) {
            if(name.contains("/")) {
                name = name.substring(0, name.lastIndexOf("/"));
            }
            else if(name.contains("\\")) {
                name = name.substring(0, name.lastIndexOf("\\"));
            }
        }
        String result = uncompressedDirectory + name;
        return result;
    }
}
