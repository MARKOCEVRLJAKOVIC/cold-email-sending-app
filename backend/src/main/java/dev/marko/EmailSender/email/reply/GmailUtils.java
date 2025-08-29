package dev.marko.EmailSender.email.reply;

import com.google.api.services.gmail.model.MessagePartHeader;
import com.google.api.services.gmail.model.Message;

public class GmailUtils {
    public static String getHeader(Message msg, String name) {
        if (msg.getPayload() == null || msg.getPayload().getHeaders() == null) return null;
        for (MessagePartHeader header : msg.getPayload().getHeaders()) {
            if (header.getName().equalsIgnoreCase(name)) {
                return header.getValue();
            }
        }
        return null;
    }
}