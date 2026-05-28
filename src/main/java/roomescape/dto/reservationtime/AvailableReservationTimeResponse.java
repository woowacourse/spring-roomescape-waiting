package roomescape.dto.reservationtime;

import roomescape.domain.reservationtime.AvailableReservationTime;

import java.time.LocalTime;

public class AvailableReservationTimeResponse {

    private final Long id;
    private final LocalTime startAt;
    private final Boolean isAvailable;

    private AvailableReservationTimeResponse(Long id, LocalTime startAt, Boolean isAvailable) {
        this.id = id;
        this.startAt = startAt;
        this.isAvailable = isAvailable;
    }

    public static AvailableReservationTimeResponse from(AvailableReservationTime availableReservationTime) {
        return new AvailableReservationTimeResponse(availableReservationTime.getId(), availableReservationTime.getStartAt(), availableReservationTime.isAvailable());
    }

    public Long getId() {
        return id;
    }

    public LocalTime getStartAt() {
        return startAt;
    }

    public Boolean getIsAvailable() {
        return isAvailable;
    }
}
