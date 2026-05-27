package roomescape.domain;

import roomescape.exception.CustomException;
import roomescape.exception.ErrorCode;

public class Reservation {

    private final Long id;
    private final String name;
    private final Long reservationId;

    public Reservation(Long id, String name, Long reservationId) {
        validateName(name);
        
        this.id = id;
        this.name = name;
        this.reservationId = reservationId;
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new CustomException(ErrorCode.RESERVATION_NAME_BLANK);
        }

        if (name.length() > 255) {
            throw new CustomException(ErrorCode.RESERVATION_NAME_TOO_LONG);
        }
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Long getReservationId() {
        return reservationId;
    }
}
