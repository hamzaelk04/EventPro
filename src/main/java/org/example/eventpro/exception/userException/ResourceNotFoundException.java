package org.example.eventpro.exception.userException;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resource, Object id) {
        super(resource + " introuvable avec l'identifiant : " + id);
    }
}
