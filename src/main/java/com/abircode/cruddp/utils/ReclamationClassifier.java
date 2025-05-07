package com.abircode.cruddp.utils;

import ai.djl.ModelException;
import ai.djl.inference.Predictor;
import ai.djl.modality.Classifications;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelZoo;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.translate.TranslateException;

import java.io.IOException;

public class ReclamationClassifier implements AutoCloseable {

    private final ZooModel<String, Classifications> model;
    private final Predictor<String, Classifications> predictor;

    public ReclamationClassifier() {
        try {
            Criteria<String, Classifications> criteria = Criteria.builder()

                    .optModelUrls("djl://ai.djl.huggingface.pytorch/nlptown/bert-base-multilingual-uncased-sentiment")
                    .optEngine("PyTorch")
                    .setTypes(String.class, Classifications.class)
                    .build();

            this.model = ModelZoo.loadModel(criteria);
            this.predictor = model.newPredictor();
        } catch (ModelException | IOException e) {
            throw new RuntimeException("Failed to load sentiment model", e);
        }
    }


    public String classifyUrgency(String text) {
        if (text == null || text.isBlank()) {
            return "MEDIUM";
        }
        try {
            Classifications res = predictor.predict(text);
            String label = res.best().getClassName();
            int stars = parseStars(label);
            if (stars <= 2) {
                return "HIGH";
            } else if (stars == 3) {
                return "MEDIUM";
            } else {
                return "LOW";
            }
        } catch (TranslateException e) {
            throw new RuntimeException("Error during sentiment prediction", e);
        }
    }

    private int parseStars(String label) {
        try {
            String[] parts = label.split(" ");
            return Integer.parseInt(parts[0]);
        } catch (Exception ignored) {
            return 3;
        }
    }

    @Override
    public void close() {
        if (predictor != null) {
            predictor.close();
        }
        if (model != null) {
            model.close();
        }
    }
}
