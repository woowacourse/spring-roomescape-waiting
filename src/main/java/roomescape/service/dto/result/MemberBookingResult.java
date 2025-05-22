package roomescape.service.dto.result;

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
        BookingType bookingType,
        long rank
) {

    public static MemberBookingResult from(Reservation reservation) {
        return new MemberBookingResult(
                reservation.getId(),
                MemberResult.from(reservation.getMember()),
                ThemeResult.from(reservation.getTheme()), reservation.getDate(),
                ReservationTimeResult.from(reservation.getTime()),
                BookingType.RESERVED,
                0
        );
    }

    public static MemberBookingResult from(Waiting waiting, long rank) {
        return new MemberBookingResult(
                waiting.getId(),
                MemberResult.from(waiting.getMember()),
                ThemeResult.from(waiting.getTheme()),
                waiting.getDate(),
                ReservationTimeResult.from(waiting.getTime()),
                BookingType.WAITED,
                rank
        );
    }

    public LocalDateTime reservationDateTime() {
        return LocalDateTime.of(date, time.startAt());
    }
}
