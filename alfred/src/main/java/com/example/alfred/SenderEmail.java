package com.example.alfred;
import javax.mail.*;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import java.io.File;
import java.util.Properties;


public class SenderEmail {
    public static void sendEmail(String toEmail, String subject, String body) {
        // Данные отправителя
        final String fromEmail = "mmoldabekbekzat@gmail.com"; // Ваш Gmail
        final String password = "yplhmzjwdhaoyuqc";


        Properties properties = new Properties();
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");


        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, password);
            }
        });

        try {

            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
            message.setSubject(subject);
            message.setText(body);

            Transport.send(message);
            System.out.println("Письмо успешно отправлено!");
        } catch (MessagingException e) {
            e.printStackTrace();
            System.out.println("Ошибка при отправке письма: " + e.getMessage());
        }
    }

    public static void sendEmailWithPhoto(String toEmail, String subject, String body, String photoPath) {
            final String fromEmail = "mmoldabekbekzat@gmail.com";
            final String appPassword = "yplhmzjwdhaoyuqc";

            Properties properties = new Properties();
            properties.put("mail.smtp.host", "smtp.gmail.com");
            properties.put("mail.smtp.port", "587");
            properties.put("mail.smtp.auth", "true");
            properties.put("mail.smtp.starttls.enable", "true");

            Session session = Session.getInstance(properties, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(fromEmail, appPassword);
                }
            });

            try {
                MimeMessage message = new MimeMessage(session);
                message.setFrom(new InternetAddress(fromEmail));
                message.addRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
                message.setSubject(subject);


                Multipart multipart = new MimeMultipart();

                MimeBodyPart textPart = new MimeBodyPart();
                textPart.setText(body);
                multipart.addBodyPart(textPart);

                MimeBodyPart photoPart = new MimeBodyPart();
                photoPart.attachFile(new File(photoPath)); // File path to the photo
                multipart.addBodyPart(photoPart);


                message.setContent(multipart);

                Transport.send(message);
                System.out.println("Email with photo sent successfully!");

            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Error while sending email with photo: " + e.getMessage());
            }
        }


}
