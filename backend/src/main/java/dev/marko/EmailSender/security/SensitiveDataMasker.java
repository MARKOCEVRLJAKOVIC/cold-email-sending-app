package dev.marko.EmailSender.security;

import org.springframework.stereotype.Component;

@Component
public class SensitiveDataMasker {
    
    public String maskEmail(String email) {
        if (email == null || !email.contains("@")) return "***@***";
        
        String[] parts = email.split("@");
        String local = parts[0];
        String domain = parts[1];
        
        String maskedLocal = local.length() > 2 
            ? local.substring(0, 2) + "***" 
            : "***";
        
        return maskedLocal + "@" + domain;
    }
    
    public String maskToken(String token) {
        if (token == null || token.length() < 8) return "***";
        return token.substring(0, 4) + "***" + token.substring(token.length() - 4);
    }
}
