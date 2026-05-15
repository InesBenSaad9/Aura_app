package tn.esprit.aura.utils;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class MailService {

    private static final String SENDER_EMAIL = "inesfatnassi099@gmail.com";
    private static final String APP_PASSWORD = "fzkhfsvrmrcnjait";

    public static void sendOTP(String recipientEmail, String otp) throws MessagingException {
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.starttls.required", "true");
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");
        properties.put("mail.smtp.ssl.trust", "smtp.gmail.com");
        properties.put("mail.smtp.ssl.protocols", "TLSv1.2");

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SENDER_EMAIL, APP_PASSWORD);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(SENDER_EMAIL));
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipientEmail));
        message.setSubject("AURA — Reinitialisation du mot de passe OTP");
        
        String htmlContent = "<div style='font-family: Arial, sans-serif; background-color: #0A0C0F; color: #F0F4F8; padding: 40px; border-radius: 12px;'>" +
                "<h1 style='color: #1BBFA8;'>Reinitialiser votre mot de passe</h1>" +
                "<p>Bonjour,</p>" +
                "<p>Vous avez demande la reinitialisation du mot de passe de votre compte AURA. Utilisez le code suivant :</p>" +
                "<div style='background-color: #111418; padding: 20px; border-radius: 8px; text-align: center; margin: 30px 0; border: 1px solid #1BBFA8;'>" +
                "<span style='font-size: 32px; font-weight: 800; letter-spacing: 8px; color: #1BBFA8;'>" + otp + "</span>" +
                "</div>" +
                "<p>Ce code expire dans 10 minutes. Si vous n etes pas a l origine de cette demande, ignorez cet email.</p>" +
                "<br><p>Cordialement,<br>L equipe AURA</p>" +
                "</div>";

        message.setContent(htmlContent, "text/html");

        Transport.send(message);
    }

    public static void sendWelcomeEmail(String recipientEmail, String nom) throws MessagingException {
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.starttls.required", "true");
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");
        properties.put("mail.smtp.ssl.trust", "smtp.gmail.com");
        properties.put("mail.smtp.ssl.protocols", "TLSv1.2");

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SENDER_EMAIL, APP_PASSWORD);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(SENDER_EMAIL));
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipientEmail));
        message.setSubject("Bienvenue sur AURA - Votre experience commence ici");

        String htmlContent = "<div style='font-family: \"Segoe UI\", Roboto, Helvetica, Arial, sans-serif; background-color: #0A0C0F; color: #F0F4F8; padding: 0; margin: 0;'>" +
                "<div style='max-width: 600px; margin: 0 auto; background-color: #111418; border-radius: 16px; overflow: hidden; border: 1px solid #1BBFA822;'>" +
                "    <div style='background: linear-gradient(135deg, #1BBFA8 0%, #16a085 100%); padding: 40px 20px; text-align: center;'>" +
                "        <h1 style='color: #ffffff; margin: 0; font-size: 32px; letter-spacing: 2px; text-transform: uppercase;'>AURA</h1>" +
                "    </div>" +
                "    <div style='padding: 40px;'>" +
                "        <h2 style='color: #1BBFA8; margin-top: 0;'>Bienvenue, " + nom + "! 🚀</h2>" +
                "        <p style='font-size: 16px; line-height: 1.6; color: #B0BCC8;'>Nous sommes ravis de vous accueillir dans la communaute AURA. Votre compte a ete cree avec succes.</p>" +
                "        <div style='margin: 30px 0; padding: 20px; background-color: #0A0C0F; border-radius: 12px; border-left: 4px solid #1BBFA8;'>" +
                "            <p style='margin: 0; font-style: italic; color: #F0F4F8;'>\"AURA est votre acces personnel a une experience simple et securisee.\"</p>" +
                "        </div>" +
                "        <p style='font-size: 16px; line-height: 1.6; color: #B0BCC8;'>Voici ce que vous pouvez faire maintenant :</p>" +
                "        <ul style='color: #B0BCC8; padding-left: 20px;'>" +
                "            <li style='margin-bottom: 10px;'>Completez votre profil avec une bio et une photo</li>" +
                "            <li style='margin-bottom: 10px;'>Configurez Face ID pour une connexion plus rapide</li>" +
                "            <li style='margin-bottom: 10px;'>Explorez les fonctionnalites du tableau de bord</li>" +
                "        </ul>" +
                "        <div style='text-align: center; margin: 40px 0;'>" +
                "            <a href='#' style='background-color: #1BBFA8; color: #ffffff; padding: 16px 32px; text-decoration: none; border-radius: 8px; font-weight: bold; display: inline-block; box-shadow: 0 4px 15px rgba(27, 191, 168, 0.3);'>Commencer maintenant</a>" +
                "        </div>" +
                "        <hr style='border: 0; border-top: 1px solid #1BBFA822; margin: 40px 0;'>" +
                "        <p style='font-size: 14px; text-align: center; color: #64748B;'>Pour toute question, vous pouvez repondre a cet email. Notre equipe d assistance reste disponible.</p>" +
                "        <p style='font-size: 14px; text-align: center; color: #64748B; margin-top: 20px;'>Cordialement,<br><strong style='color: #1BBFA8;'>L equipe AURA</strong></p>" +
                "    </div>" +
                "    <div style='background-color: #0A0C0F; padding: 20px; text-align: center; font-size: 12px; color: #475569;'>" +
                "        &copy; 2024 AURA Inc. Tous droits reserves.<br>" +
                "        Esprit, Ariana, Tunisia" +
                "    </div>" +
                "</div>" +
                "</div>";

        message.setContent(htmlContent, "text/html");
        Transport.send(message);
    }
}
