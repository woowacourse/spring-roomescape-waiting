package roomescape.reservation.domain;

import java.util.ResourceBundle;

public enum ReservationStatus {
    BOOKED,
    WAITING,
    ;

    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("reservationStatus");

    public String getOutput() {
        return RESOURCE_BUNDLE.getString(name());
    }
}
