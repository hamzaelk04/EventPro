package org.example.eventpro.exception.userException;

public class EmailAlreadyUsedException extends RuntimeException {
    public EmailAlreadyUsedException(String email) {
        super("L'adresse email est déjà utilisée : " + email);
    }
}
