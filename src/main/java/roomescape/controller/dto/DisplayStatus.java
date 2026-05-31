package roomescape.controller.dto;

import roomescape.domain.ReservationStatus;

import java.time.LocalDateTime;

public enum DisplayStatus {

    RESERVED, WAITING, CANCELED, COMPLETED;

    public static DisplayStatus from(
            ReservationStatus status,
            LocalDateTime now,
            LocalDateTime reservationDateTime
    ) {
        if (status == ReservationStatus.RESERVED && reservationDateTime.isBefore(now)) {
            return COMPLETED;
        }

        return DisplayStatus.valueOf(status.name());
    }
}
