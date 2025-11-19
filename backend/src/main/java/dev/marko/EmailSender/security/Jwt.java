package dev.marko.EmailSender.security;

import dev.marko.EmailSender.entities.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.AllArgsConstructor;

import javax.crypto.SecretKey;
import java.util.Date;

@AllArgsConstructor
public class Jwt {

    private final Claims claims;
    private final SecretKey key;

    public boolean isExpired(){
        return claims.getExpiration().before(new Date());
    }

    public Long getUserId (){
        return Long.valueOf(claims.getSubject());
    }

    public Role getRole(){
        return Role.valueOf(claims.get("role", String.class));
    }

    public String getEmail() {
        return claims.get("email", String.class);
    }

    public String toString(){
        return Jwts.builder().claims(claims).signWith(key).compact();
    }


}
