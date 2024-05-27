package roomescape.service.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import roomescape.domain.Reservation;

import java.time.LocalDate;
import java.time.LocalTime;

public record WaitingResponse(Long id,
                              String name,
                              String theme,
                              @JsonFormat(pattern = "YYYY-MM-dd") LocalDate date,
                              @JsonFormat(pattern = "HH:mm") LocalTime startAt) {

    public WaitingResponse(Reservation reservation) {
        this(
                reservation.getId(),
                reservation.getMember().getName(),
                reservation.getTheme().getName(),
                reservation.getDate(),
                reservation.getReservationTime().getStartAt()
        );
    }
}
