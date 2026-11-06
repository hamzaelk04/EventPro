package org.example.eventpro.exception.reservationException;

public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException(String message) {
        super(message);
    }

    public InsufficientStockException(String categoryName, int requested, int available) {
        super("Stock insuffisant pour la catégorie « " + categoryName + " » : "
                + requested + " place(s) demandée(s), " + available + " disponible(s)");
    }
}