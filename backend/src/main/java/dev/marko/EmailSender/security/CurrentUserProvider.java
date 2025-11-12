package dev.marko.EmailSender.security;

import dev.marko.EmailSender.entities.User;

public interface CurrentUserProvider {
    User getCurrentUser();
}
