package com.example.scraper.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

public final class CategoryEncoder {

    public static String encodeToBase64(List<String> categories) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("job_functions", new Gson().toJsonTree(categories));
        String jsonString = jsonObject.toString();
        byte[] jsonBytes = jsonString.getBytes(StandardCharsets.UTF_8);
        byte[] base64Bytes = Base64.getEncoder().encode(jsonBytes);

        return new String(base64Bytes, StandardCharsets.UTF_8);
    }
}
