package com.abircode.cruddp.services;

import com.abircode.cruddp.entities.Event;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Events;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class GoogleCalendarService {

    private static final String APPLICATION_NAME = "Google Calendar API Java";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String CREDENTIALS_FILE_PATH = "/client_secret.json"; // Chemin vers votre fichier client_secret.json
    private static final String TOKENS_DIRECTORY_PATH = "/tokens"; // Répertoire pour stocker les tokens
    private static final String CALENDAR_ID = "eca03a781dec07a2d442ff50a29812ec94676d06fc6e5e1a9fed5a3dd506e5f6@group.calendar.google.com"; // Remplacez par votre ID de calendrier

    private static Calendar getCalendarService() throws IOException, GeneralSecurityException {
        // Lecture du fichier de service pour obtenir les credentials
        FileInputStream serviceAccountStream = new FileInputStream(TOKENS_DIRECTORY_PATH + CREDENTIALS_FILE_PATH);

        // Créez l'objet GoogleCredential à partir du flux du fichier
        GoogleCredential credential = GoogleCredential.fromStream(serviceAccountStream)
                .createScoped(Collections.singleton(CalendarScopes.CALENDAR));

        // Retourner le service Calendar configuré
        return new Calendar.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JSON_FACTORY,
                credential
        ).setApplicationName(APPLICATION_NAME).build();
    }

    public static List<Event> recupererEvenements() throws IOException, GeneralSecurityException {
        // Récupérer le service Google Calendar
        Calendar service = getCalendarService();

        LocalDateTime now = LocalDateTime.now();
        DateTime startTime = new DateTime(Date.from(now.atZone(ZoneId.systemDefault()).toInstant()));

        // Récupérer les événements à partir de Google Calendar
        Events googleEvents = service.events().list(CALENDAR_ID)
                .setTimeMin(startTime)
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .execute();

        List<Event> eventList = new ArrayList<>();

        if (googleEvents.getItems() != null) {
            for (com.google.api.services.calendar.model.Event gEvent : googleEvents.getItems()) {
                LocalDateTime startDate = null;
                LocalDateTime endDate = null;

                if (gEvent.getStart() != null && gEvent.getStart().getDateTime() != null) {
                    startDate = gEvent.getStart().getDateTime().getValue() != 0 ?
                            LocalDateTime.ofInstant(
                                    new Date(gEvent.getStart().getDateTime().getValue()).toInstant(),
                                    ZoneId.systemDefault()) : null;
                }

                if (gEvent.getEnd() != null && gEvent.getEnd().getDateTime() != null) {
                    endDate = gEvent.getEnd().getDateTime().getValue() != 0 ?
                            LocalDateTime.ofInstant(
                                    new Date(gEvent.getEnd().getDateTime().getValue()).toInstant(),
                                    ZoneId.systemDefault()) : null;
                }

                Event event = new Event(
                        0, // id pas disponible depuis Google Calendar, mettre 0
                        startDate,
                        endDate,
                        gEvent.getSummary() != null ? gEvent.getSummary() : "Sans titre",
                        gEvent.getDescription() != null ? gEvent.getDescription() : "Pas de description",
                        "", // pas d'image depuis Google Calendar
                        "", // pas d'image2
                        0 // user_id pas connu ici
                );

                eventList.add(event);
            }
        }

        return eventList;
    }
}
