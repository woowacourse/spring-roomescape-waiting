package roomescape.service.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalTime;

import roomescape.domain.ReservationTime;

public record ReservationTimeResponse(Long id,
                                      @JsonFormat(pattern = "HH:mm") LocalTime startAt) {

    public ReservationTimeResponse(ReservationTime reservationTime) {
        this(reservationTime.getId(), reservationTime.getStartAt());
    }
}
