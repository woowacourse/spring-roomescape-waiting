package roomescape.service.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import roomescape.domain.Reservation;

public record ReservationResponse(Long id, String name,
                                  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
                                  LocalDate date, MemberResponse member,
                                  ReservationTimeResponse time, ThemeResponse theme) {

    public static ReservationResponse from(Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getName(),
                reservation.getReservationDate(),
                MemberResponse.from(reservation.getMember()),
                ReservationTimeResponse.from(reservation.getTime()),
                ThemeResponse.from(reservation.getTheme())
        );
    }
}
