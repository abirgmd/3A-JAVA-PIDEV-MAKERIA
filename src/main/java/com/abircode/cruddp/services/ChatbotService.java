package com.abircode.cruddp.services;

import com.abircode.cruddp.entities.Categorie;
import com.abircode.cruddp.entities.OrdersDetails;
import com.abircode.cruddp.entities.Produit;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class ChatbotService {

    private final ServiceProduit produitService = new ServiceProduit();
    private final CartService cartService = CartService.getInstance();
    private final ServiceCategorie categorieService = new ServiceCategorie();

    private static final String OPENROUTER_API_URL = "https://openrouter.ai/api/v1/chat/completions";

    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String getResponse(String userMessage) {
        StringBuilder finalResponse = new StringBuilder();

        try {
            boolean handled = false;

            if (isProductListQuestion(userMessage)) {
                finalResponse.append(getProductListFromDatabase()).append("\n\n");
                handled = true;
            }

            if (isProductDetailQuestion(userMessage)) {
                finalResponse.append(getProductDetailFromDatabase(userMessage)).append("\n\n");
                handled = true;
            }

            if (isCartQuestion(userMessage)) {
                finalResponse.append(getCartInfoFromDatabase()).append("\n\n");
                handled = true;
            }

            if (isCategoryListQuestion(userMessage)) {
                finalResponse.append(getCategoryListFromDatabase()).append("\n\n");
                handled = true;
            }

            if (handled) {
                // Appel à Gemini pour compléter ou reformuler la réponse
                finalResponse.append(getOpenRouterResponse(userMessage));
                return finalResponse.toString().trim();
            } else {
                // Sinon, Gemini traite tout seul
                return getOpenRouterResponse(userMessage);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "Désolé, une erreur s'est produite. Veuillez réessayer plus tard.";
        }
    }

    private boolean isProductListQuestion(String msg) {
        msg = msg.toLowerCase();
        return msg.contains("produits disponibles") || msg.contains("liste des produits");
    }

    private boolean isProductDetailQuestion(String msg) {
        msg = msg.toLowerCase();
        return msg.contains("détail du produit") || msg.contains("informations sur") || msg.contains("description de");
    }

    private boolean isCartQuestion(String msg) {
        msg = msg.toLowerCase();
        return msg.contains("mon panier") || msg.contains("contenu du panier") || msg.contains("total du panier");
    }

    private boolean isCategoryListQuestion(String msg) {
        msg = msg.toLowerCase();
        return msg.contains("catégories disponibles") || msg.contains("liste des catégories") || msg.contains("quelles sont les catégories");
    }

    private String getProductListFromDatabase() {
        try {
            List<Produit> produits = produitService.afficher();
            if (produits.isEmpty()) return "Aucun produit n'est disponible pour le moment.";
            return "Produits disponibles :\n- " +
                    produits.stream().map(Produit::getNom).collect(Collectors.joining("\n- "));
        } catch (SQLException e) {
            e.printStackTrace();
            return "Erreur lors de la récupération des produits.";
        }
    }

    private String getProductDetailFromDatabase(String userMessage) {
        try {
            String productName = extractProductName(userMessage);
            if (productName == null) return "Merci de préciser le nom du produit.";

            for (Produit p : produitService.afficher()) {
                if (p.getNom().equalsIgnoreCase(productName)) {
                    return "Détail du produit \"" + p.getNom() + "\" :\n" +
                            "Description : " + p.getDescription() + "\n" +
                            "Prix : " + p.getPrix() + " TND\n" +
                            "Stock : " + p.getStock();
                }
            }
            return "Produit \"" + productName + "\" non trouvé.";
        } catch (SQLException e) {
            e.printStackTrace();
            return "Erreur lors de la récupération des détails du produit.";
        }
    }

    private String getCartInfoFromDatabase() {
        List<OrdersDetails> items = cartService.getCartItems();
        if (items.isEmpty()) return "Votre panier est vide.";
        double total = items.stream().mapToDouble(OrdersDetails::getPrice).sum();

        StringBuilder sb = new StringBuilder("Votre panier contient :\n");
        for (OrdersDetails p : items) {
            sb.append("- ").append(p.getProduit().getNom())
                    .append(" : ").append(p.getProduit().getPrix())
                    .append(" TND\n");
        }
        sb.append("Total : ").append(total).append(" TND");
        return sb.toString();
    }

    private String getCategoryListFromDatabase() {
        try {
            List<Categorie> categories = categorieService.afficher();
            if (categories.isEmpty()) return "Aucune catégorie n'est disponible pour le moment.";
            return "Catégories disponibles :\n- " +
                    categories.stream().map(Categorie::getNomcat).collect(Collectors.joining("\n- "));
        } catch (SQLException e) {
            e.printStackTrace();
            return "Erreur lors de la récupération des catégories.";
        }
    }

    private String extractProductName(String userMessage) {
        String msg = userMessage.toLowerCase();
        if (msg.contains("détail du produit")) {
            return userMessage.substring(msg.indexOf("détail du produit") + 17).trim();
        }
        if (msg.contains("description de")) {
            return userMessage.substring(msg.indexOf("description de") + 14).trim();
        }
        if (msg.contains("informations sur")) {
            return userMessage.substring(msg.indexOf("informations sur") + 17).trim();
        }
        return null;
    }

    private String getOpenRouterResponse(String userMessage) throws Exception {
        String jsonBody = "{"
                + "\"model\":\"google/gemini-2.5-pro-preview-03-25\","
                + "\"max_tokens\":512,"
                + "\"messages\":[{\"role\":\"user\",\"content\":\"" + userMessage.replace("\"", "\\\"") + "\"}]"
                + "}";

        Request request = new Request.Builder()
                .url(OPENROUTER_API_URL)
                .addHeader("Authorization", "Bearer " + OPENROUTER_API_KEY)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(jsonBody, MediaType.parse("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            String body = response.body().string();
            if (!response.isSuccessful()) return "Erreur IA (" + response.code() + ") : " + body;

            return objectMapper.readTree(body)
                    .get("choices").get(0).get("message").get("content")
                    .asText().trim();
        }
    }

    public void clearHistory() {

    }
}
