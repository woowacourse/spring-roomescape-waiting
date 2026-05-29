package roomescape.web.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import roomescape.dao.dto.WaitingWithRank;
import roomescape.domain.Reservation;

import java.time.LocalDate;
import java.time.LocalTime;

public record ReservationResponse(
        Long id,
        String name,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
        LocalDate date,

        TimeInfo time,
        ThemeInfo theme,

        String status,
        Integer rank
) {
    public static ReservationResponse from(Reservation reservation) {
        return new ReservationResponse(
                reservation.id(),
                reservation.username(),
                reservation.reservationDate(),
                new TimeInfo(reservation.reservationTime().id(), reservation.reservationTime().startAt()),
                new ThemeInfo(
                        reservation.reservationTheme().id(),
                        reservation.reservationTheme().name(),
                        reservation.reservationTheme().thumbnailUrl(),
                        reservation.reservationTheme().description()),
                "예약",
                null
        );
    }

    public static ReservationResponse from(WaitingWithRank waiting) {
        return new ReservationResponse(
                waiting.id(),
                waiting.name(),
                waiting.reservationDate(),
                new TimeInfo(waiting.reservationTime().id(), waiting.reservationTime().startAt()),
                new ThemeInfo(
                        waiting.reservationTheme().id(),
                        waiting.reservationTheme().name(),
                        waiting.reservationTheme().thumbnailUrl(),
                        waiting.reservationTheme().description()),
                "예약대기",
                waiting.rank()
        );
    }

    private record TimeInfo(
            Long id,

            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm", timezone = "Asia/Seoul")
            LocalTime startAt) {

    }

    private record ThemeInfo(Long id,
                             String name,
                             String thumbnailUrl,
                             String description
    ) {
    }
}
