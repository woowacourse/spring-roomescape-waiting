package roomescape.reservation.controller.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.domain.Reservation;

public record MemberReservationResponse(
        long id,
        LocalDate date,
        @JsonFormat(pattern = "kk:mm")
        LocalTime startAt,
        String themeName,
        String status
) {

    public static MemberReservationResponse of(final Reservation reservation, final int count) {
        return new MemberReservationResponse(
                reservation.getId(),
                reservation.getDate(),
                reservation.getTime().getStartAt(),
                reservation.getTheme().getThemeNameValue(),
                makeStatus(count)
        );
    }

    private static String makeStatus(final int count) {
        if (count == 0) {
            return "예약";
        }
        return String.format("%d번째 %s", count, "예약대기");
    }
}
