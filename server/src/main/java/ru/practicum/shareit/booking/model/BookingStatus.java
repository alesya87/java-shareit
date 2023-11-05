package ru.practicum.shareit.booking.model;

public enum BookingStatus {
    WAITING("WAITING"),
    APPROVED("APPROVED"),
    REJECTED("REJECTED"),
    CANCELED("CANCELED"),
    CURRENT("CURRENT"),
    PAST("PAST"),
    ALL("ALL"),
    FUTURE("FUTURE");

    private String value;

    BookingStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
