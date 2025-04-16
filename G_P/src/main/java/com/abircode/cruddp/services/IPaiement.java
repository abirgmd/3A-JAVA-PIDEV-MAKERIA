package com.abircode.cruddp.services;

public interface IPaiement<T> {
    void creatPaiement(T t);
    void updatePaiement(T t); // ✅ ici !
    void deletePaiement(int id);
    void selectPaiement();
    void viderChamps(javafx.event.ActionEvent event);
    java.util.List<T> showPaiements();
}
