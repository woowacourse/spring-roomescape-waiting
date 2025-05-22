package roomescape.service.result;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import roomescape.domain.Reservation;
import roomescape.domain.Waiting;

public record MemberBookingResult(
        Long id,
        String memberName,
        LocalDate date,
        LocalTime time,
        String themeName,
        String status
        ) {

    public static MemberBookingResult from(Reservation reservation) {
        return new MemberBookingResult(
                reservation.getId(),
                reservation.getMember().getName(),
                reservation.getDate(),
                reservation.getTime().getStartAt(),
                reservation.getTheme().getName(),
                "예약"
        );
    }

    public static MemberBookingResult from(Waiting waiting) {
        return new MemberBookingResult(
                waiting.getId(),
                waiting.getMember().getName(),
                waiting.getDate(),
                waiting.getTime().getStartAt(),
                waiting.getTheme().getName(),
                "대기"
        );
    }

    public LocalDateTime reservationDateTime() {
        return LocalDateTime.of(date, time);
    }
}
