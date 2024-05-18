package roomescape.service.dto.time;

import roomescape.domain.reservation.ReservationTimeStatus;
import roomescape.service.dto.reservation.ReservationTimeResponse;

public class AvailableTimeResponse {

    private final ReservationTimeResponse timeResponseDto;
    private final boolean booked;

    public AvailableTimeResponse(ReservationTimeResponse timeResponseDto, boolean booked) {
        this.timeResponseDto = timeResponseDto;
        this.booked = booked;
    }

    public AvailableTimeResponse(ReservationTimeStatus reservationTimeStatus) {
        this(new ReservationTimeResponse(reservationTimeStatus.getReservationTime()),
                reservationTimeStatus.getReservationStatus().getStatus()
        );
    }

    public ReservationTimeResponse getTimeResponseDto() {
        return timeResponseDto;
    }

    public boolean isBooked() {
        return booked;
    }
}
