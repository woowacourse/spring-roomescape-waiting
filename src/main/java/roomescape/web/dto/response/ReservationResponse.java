package roomescape.web.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import roomescape.domain.service.WaitingWithRank;
import roomescape.domain.reservation.Reservation;

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
                reservation.getId(),
                reservation.getUserName().getName(),
                reservation.getReservationDate(),
                new TimeInfo(reservation.getReservationTime().getId(), reservation.getReservationTime().getStartAt()),
                new ThemeInfo(
                        reservation.getReservationTheme().getId(),
                        reservation.getReservationTheme().getThemeName(),
                        reservation.getReservationTheme().getThumbnailUrl(),
                        reservation.getReservationTheme().getDescription()),
                "예약",
                null
        );
    }

    public static ReservationResponse from(WaitingWithRank waiting) {
        return new ReservationResponse(
                waiting.id(),
                waiting.name().getName(),
                waiting.reservationDate(),
                new TimeInfo(waiting.reservationTime().getId(), waiting.reservationTime().getStartAt()),
                new ThemeInfo(
                        waiting.reservationTheme().getId(),
                        waiting.reservationTheme().getThemeName(),
                        waiting.reservationTheme().getThumbnailUrl(),
                        waiting.reservationTheme().getDescription()),
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
