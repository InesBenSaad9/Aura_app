package org.example.utils;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

public class TwilioService {

    private static TwilioService instance;

    private static final String ACCOUNT_SID = "ACd942372f8786dc4d8ff3f9ad22e1e15f";
    private static final String AUTH_TOKEN  = "da4ae1901980a770a6d5579980dec841";
    private static final String FROM_PHONE  = "+19787362579"; // ex: +12295099944

    private TwilioService() {}

    public static TwilioService getInstance() {
        if (instance == null) instance = new TwilioService();
        return instance;
    }

    public void init() {
        try {
            Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
            System.out.println("✅ Twilio initialisé");
        } catch (Exception e) {
            System.out.println("❌ Erreur Twilio : " + e.getMessage());
        }
    }

    public void sendSMS(String toPhone, String message) {
        try {
            Message.creator(
                    new PhoneNumber(toPhone),
                    new PhoneNumber(FROM_PHONE),
                    message
            ).create();
            System.out.println("✅ SMS envoyé à " + toPhone);
        } catch (Exception e) {
            System.out.println("❌ Erreur envoi SMS : " + e.getMessage());
        }
    }

    public void sendWhatsApp(String toPhone, String message) {
        try {
            Message.creator(
                    new PhoneNumber("whatsapp:" + toPhone),
                    new PhoneNumber("whatsapp:" + FROM_PHONE),
                    message
            ).create();
            System.out.println("✅ WhatsApp envoyé à " + toPhone);
        } catch (Exception e) {
            System.out.println("❌ Erreur WhatsApp : " + e.getMessage());
        }
    }
}