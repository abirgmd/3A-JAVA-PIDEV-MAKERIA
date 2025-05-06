package com.abircode.cruddp.services.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class DeepLTranslationService {
    private static final String DEEPL_API_URL = "https://api-free.deepl.com/v2/translate";
    private final String apiKey;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public DeepLTranslationService(String apiKey) {
        this.apiKey = apiKey;
    }

    public String translate(String text, String targetLanguage) throws IOException {
        if (text == null || text.isEmpty()) {
            return text;
        }

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost request = new HttpPost(DEEPL_API_URL);

            // Set headers
            request.setHeader("Authorization", "DeepL-Auth-Key " + apiKey);
            request.setHeader("Content-Type", "application/json");

            // Create request body
            String requestBody = String.format(
                    "{\"text\":[\"%s\"],\"target_lang\":\"%s\"}",
                    text.replace("\"", "\\\""),
                    targetLanguage
            );

            request.setEntity(new StringEntity(requestBody));

            // Execute request
            CloseableHttpResponse response = httpClient.execute(request);
            String responseBody = EntityUtils.toString(response.getEntity());

            // Parse response
            Map<String, Object> result = objectMapper.readValue(responseBody, Map.class);
            List<Map<String, String>> translations = (List<Map<String, String>>) result.get("translations");

            return translations.get(0).get("text");
        }
    }
}