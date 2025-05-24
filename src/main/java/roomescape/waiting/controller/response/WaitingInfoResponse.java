package roomescape.waiting.controller.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.domain.Reservation;

public record WaitingInfoResponse(Long id,
                                  String name,
                                  String theme,
                                  @JsonFormat(pattern = "yyyy-MM-dd") LocalDate date,
                                  LocalTime startAt
) {
    public static WaitingInfoResponse from(Reservation reservation) {
        return new WaitingInfoResponse(
                reservation.getId(),
                reservation.getReserverName(),
                reservation.getThemeName(),
                reservation.getDate(),
                reservation.getStartAt()
        );
    }
}
