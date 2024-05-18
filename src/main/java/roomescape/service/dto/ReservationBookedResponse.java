package roomescape.service.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import java.time.LocalDate;
import java.time.LocalDateTime;
import roomescape.domain.reservation.dto.ReservationReadOnly;

public record ReservationBookedResponse(
        Long id,
        MemberResponse member,
        @JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
        LocalDate date,
        ReservationTimeResponse time,
        ThemeResponse theme,
        String status
) {

    public static ReservationBookedResponse from(ReservationReadOnly reservation) {
        return new ReservationBookedResponse(
                reservation.id(),
                MemberResponse.from(reservation.member()),
                reservation.date(),
                new ReservationTimeResponse(reservation.time()),
                new ThemeResponse(reservation.theme()),
                "예약 확정"
        );
    }

    public LocalDateTime dateTime() {
        return LocalDateTime.of(date, time.startAt());
    }
}
