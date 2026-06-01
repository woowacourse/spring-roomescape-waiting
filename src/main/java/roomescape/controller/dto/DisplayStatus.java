package roomescape.controller.dto;

import roomescape.domain.ReservationStatus;

import java.time.LocalDateTime;

public enum DisplayStatus {

    RESERVED, WAITING, CANCELED, COMPLETED, EXPIRED;

    public static DisplayStatus from(
            ReservationStatus status,
            LocalDateTime now,
            LocalDateTime reservationDateTime
    ) {
        if (status == ReservationStatus.RESERVED && !reservationDateTime.isAfter(now)) {
            return COMPLETED;
        }

        if (status == ReservationStatus.WAITING && !reservationDateTime.isAfter(now)) {
            return EXPIRED;
        }

        return DisplayStatus.valueOf(status.name());
    }
}
