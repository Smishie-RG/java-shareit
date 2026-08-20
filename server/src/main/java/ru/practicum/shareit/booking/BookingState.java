package ru.practicum.shareit.booking;

import java.util.Locale;

public enum BookingState {
    ALL,
    CURRENT,
    PAST,
    FUTURE,
    WAITING,
    REJECTED;

    public static BookingState from(String value) {
        return BookingState.valueOf(value.toUpperCase(Locale.ROOT));
    }
}
