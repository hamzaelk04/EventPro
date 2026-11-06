package org.example.eventpro.exception.reservationException;

public class ReservationExpiredException extends RuntimeException {

    public ReservationExpiredException(String message) {
        super(message);
    }

    public ReservationExpiredException(Long reservationId) {
        super("La réservation " + reservationId + " a expiré");
    }
}