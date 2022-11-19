package com.ridemotors.tgbot.util;

import com.ridemotors.tgbot.config.BotConfig;
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

    public void extractFile(String pathFile) {
        try (var file = new ZipFile(pathFile)) {
            var entries = file.entries();
            var uncompressedDirectory = new File(file.getName()).getParent() + File.separator;
            while (entries.hasMoreElements()) {
                var entry = entries.nextElement();
                if (entry.isDirectory()) {
                    processDirectory(uncompressedDirectory, entry);
                } else {
                    processFile(file, uncompressedDirectory, entry);
                }
            }
            new File(pathFile).delete();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void processDirectory(String uncompressedDirectory, ZipEntry entry) {
        var newDirectory = uncompressedDirectory + entry.getName();
        log.info("Creating Directory: {}", newDirectory);
        createDirectory(newDirectory);
    }

    private void processFile(ZipFile file, String uncompressedDirectory, ZipEntry entry) throws IOException {
        try (
                var is = file.getInputStream(entry);
                var bis = new BufferedInputStream(is)
        ) {
            var uncompressedFileName = uncompressedDirectory + entry.getName();
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
}
