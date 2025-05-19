package roomescape.reservation.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import roomescape.reservation.domain.Reservation;
import roomescape.waiting.domain.Waiting;

public record MyReservationResponse(Long id, String theme, LocalDate date, LocalTime time, String status) {
    public static List<MyReservationResponse> of(List<Reservation> reservations, List<Waiting> waitings) {
        List<MyReservationResponse> reservationResponses = reservations.stream()
                .map(reservation -> new MyReservationResponse(reservation.getId(), reservation.getTheme().getName(),
                        reservation.getDate(), reservation.getTime().getStartAt(), "예약"))
                .toList();

        List<MyReservationResponse> waitingResponses = waitings.stream()
                .map(waiting -> new MyReservationResponse(waiting.getId(), waiting.getTheme().getName(),
                        waiting.getDate(), waiting.getTime().getStartAt(), "예약 대기"))
                .toList();

        return Stream.concat(reservationResponses.stream(), waitingResponses.stream())
                .collect(Collectors.toList());

    }


}
