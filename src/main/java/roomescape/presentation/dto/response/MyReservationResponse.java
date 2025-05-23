package roomescape.presentation.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import roomescape.domain.Reservation;
import roomescape.domain.Waiting;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public record MyReservationResponse(
        Long id,

        String theme,

        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate date,

        @JsonFormat(pattern = "HH:mm")
        LocalTime time,

        String status,

        boolean isWaiting
        ) {

    public static MyReservationResponse from(Reservation reservation) {
        return new MyReservationResponse(
                reservation.getId(),
                reservation.getTheme().getName(),
                reservation.getDate(),
                reservation.getTime().getStartAt(),
                reservation.getStatus().getName(),
                false
        );
    }

    public static MyReservationResponse from(Waiting waiting) {
        return new MyReservationResponse(
                waiting.getId(),
                waiting.getReservationInfo().getTheme().getName(),
                waiting.getReservationInfo().getDate(),
                waiting.getReservationInfo().getTime().getStartAt(),
                waiting.getRank() + "번째 예약대기",
                true
        );
    }

    public static List<MyReservationResponse> from(List<Reservation> reservations, List<Waiting> waitings) {
        return Stream.concat(
                reservations.stream().map(MyReservationResponse::from),
                waitings.stream().map(MyReservationResponse::from))
        .sorted(Comparator.comparing(MyReservationResponse::date)
                .thenComparing(MyReservationResponse::time))
        .toList();
    }
}
