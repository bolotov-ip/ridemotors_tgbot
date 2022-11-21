package com.ridemotors.tgbot.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class Util {

    public static ObjectMapper objectMapper = new ObjectMapper();

    public static Map<String, String> convertStringToMap(String str) throws JsonProcessingException {
        if(str==null || str.isBlank())
            return null;
        TypeReference<HashMap<String,String>> typeRef = new TypeReference<HashMap<String,String>>() {};
        HashMap<String,String> map = objectMapper.readValue(str, typeRef);
        return map;
    }

    public static  String convertMapToString(Map<String, String> map) throws JsonProcessingException {
        if(map == null || map.size()==0)
            return "";
        String str = objectMapper.writeValueAsString(map);
        return str;
    }

    public static Long formatStringToLong(String number) {
        if(number==null || number.isBlank())
            return 0L;
        if(number.contains("."))
            number = number.substring(0, number.indexOf("."));
        return Long.valueOf(number);
    }

    public static String doubleToString(Double number) {
        int intNumber = number.intValue();
        if(number - intNumber>0)
            return String.valueOf(number);
        String result = String.valueOf(number);
        result = result.substring(0, result.indexOf("."));
        return result;
    }
}
