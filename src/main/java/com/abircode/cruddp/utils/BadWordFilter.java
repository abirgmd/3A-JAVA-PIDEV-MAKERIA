package com.abircode.cruddp.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class BadWordFilter {
    private static final String API_URL = "https://www.purgomalum.com/service/plain?text=";
    private static final String FILL_TEXT = "[bad]";
    public static String filterBadWords(String text) {
        try {
            String urlString = API_URL + java.net.URLEncoder.encode(text, "UTF-8") + "&fill_text=" + java.net.URLEncoder.encode(FILL_TEXT, "UTF-8");
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            return response.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return text;

        }
    }
    public static int countBadWords(String filteredText) {
        return filteredText.split(FILL_TEXT).length - 1;
    }
}
