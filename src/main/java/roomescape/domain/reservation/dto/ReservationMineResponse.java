package roomescape.domain.reservation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import roomescape.domain.reservation.domain.reservation.Reservation;

import java.time.LocalDate;
import java.time.LocalTime;

public record ReservationMineResponse(Long reservationId, String theme, LocalDate date, LocalTime time, String status) {

    public ReservationMineResponse(Reservation reservation) {
        this(reservation.getId(), reservation.getTheme().getName(), reservation.getDate(), reservation.getTime().getStartAt(), "예약");
    }

    @JsonFormat(pattern = "HH:mm")
    @JsonProperty("time")
    public LocalTime getFormattedTime() {
        return time;
    }
}
