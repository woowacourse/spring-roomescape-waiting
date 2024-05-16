package roomescape.service.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import java.time.LocalDate;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationReadOnly;

public record ReservationResponse(
        Long id,
        MemberResponse member,
        @JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
        LocalDate date,
        ReservationTimeResponse time,
        ThemeResponse theme
) {

    public static ReservationResponse from(ReservationReadOnly reservation) {
        return new ReservationResponse(
                reservation.id(),
                MemberResponse.from(reservation.member()),
                reservation.date(),
                new ReservationTimeResponse(reservation.time()),
                new ThemeResponse(reservation.theme())
        );
    }

    public ReservationResponse(Reservation reservation) {
        this(
                reservation.getId(),
                MemberResponse.from(reservation.getMember()),
                reservation.getDate(),
                new ReservationTimeResponse(reservation.getTime()),
                new ThemeResponse(reservation.getTheme())
        );
    }
}
