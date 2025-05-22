package roomescape.service.result;

import java.time.LocalDate;
import java.time.LocalDateTime;
import roomescape.domain.Reservation;
import roomescape.domain.Waiting;

public record MemberBookingResult(
        Long id,
        MemberResult member,
        ThemeResult theme,
        LocalDate date,
        ReservationTimeResult time,
        String status
) {

    public static MemberBookingResult from(Reservation reservation) {
        return new MemberBookingResult(
                reservation.getId(),
                MemberResult.from(reservation.getMember()),
                ThemeResult.from(reservation.getTheme()), reservation.getDate(),
                ReservationTimeResult.from(reservation.getTime()),
                "예약"
        );
    }

    public static MemberBookingResult from(Waiting waiting) {
        return new MemberBookingResult(
                waiting.getId(),
                MemberResult.from(waiting.getMember()),
                ThemeResult.from(waiting.getTheme()), waiting.getDate(),
                ReservationTimeResult.from(waiting.getTime()),
                "대기"
        );
    }

    public LocalDateTime reservationDateTime() {
        return LocalDateTime.of(date, time.startAt());
    }
}
