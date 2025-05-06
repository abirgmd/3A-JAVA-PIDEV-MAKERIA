package com.abircode.cruddp.utils;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;
import java.util.function.Consumer;

public class EmailSender {
    private static final String SMTP_HOST = "longevityplus.store";
    private static final int SMTP_PORT = 465;
    private static final String SMTP_USERNAME = "rahma@longevityplus.store"; // Replace with your SMTP username
    private static final String SMTP_PASSWORD = "RahmaRahma123"; // Replace with your SMTP password


    public static void sendVerificationEmailAsync(String recipientEmail, String code, Consumer<Boolean> callback) {
        new Thread(() -> {
            boolean success = sendVerificationEmail(recipientEmail, code);
            callback.accept(success);
        }).start();
    }

    private static boolean sendVerificationEmail(String recipientEmail, String code) {
        try {
            Properties props = new Properties();
            props.put("mail.smtp.host", SMTP_HOST);
            props.put("mail.smtp.port", SMTP_PORT);
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.socketFactory.port", SMTP_PORT);
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

            Session session = Session.getInstance(props,
                    new Authenticator() {
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(SMTP_USERNAME, SMTP_PASSWORD);
                        }
                    });

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SMTP_USERNAME));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(recipientEmail));
            message.setSubject("Your Password Reset Code");
            message.setText("Your verification code is: " + code +
                    "\n\nThis code will expire in 15 minutes.");

            Transport.send(message);
            return true;
        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        }
    }

 }