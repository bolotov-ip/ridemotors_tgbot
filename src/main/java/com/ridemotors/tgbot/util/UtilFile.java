package com.ridemotors.tgbot.util;

import com.ridemotors.tgbot.config.BotConfig;
import com.ridemotors.tgbot.constant.STATE_UPDATE_RESOURCES;
import com.ridemotors.tgbot.service.ResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.Charset;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Service
public class UtilFile {

    @Autowired
    private BotConfig botConfig;

    private static final Logger log = LoggerFactory.getLogger(ResourceManager.class);

    public String getPathResources() {
        String path = getPathRootApp() +"resources" + File.separator;
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

    public boolean extractFile(String pathFile, String uncompressedDirectory) {
        boolean result = true;
        Charset CP866 = Charset.forName("CP866");
        try (var file = new ZipFile(pathFile, CP866)) {
            var entries = file.entries();
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
            result = false;
        }
        return result;
    }

    private void processDirectory(String uncompressedDirectory, ZipEntry entry) {
        var newDirectory = uncompressedDirectory + entry.getName();
        var directory = new File(newDirectory);
        if(directory.exists())
            deleteDirectory(directory);
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

    public void clearDirectory(String path) {
        for (File file : new File(path).listFiles())
            recursiveDelete(file);
    }

    public void deleteDirectory(File file) {
        recursiveDelete(file);
    }

    private void recursiveDelete(File file) {
        if (!file.exists())
            return;
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                recursiveDelete(f);
            }
        }
        file.delete();
    }
}
