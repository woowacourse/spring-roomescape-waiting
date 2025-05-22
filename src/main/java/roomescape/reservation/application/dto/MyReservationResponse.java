package roomescape.reservation.application.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import roomescape.reservation.domain.Reservation;
import roomescape.waiting.domain.WaitingWithRank;

public record MyReservationResponse(Long id, String theme, LocalDate date, LocalTime time, String status) {
    public static final String RESERVED = "예약";
    public static final String WAITING = "번째 예약 대기";

    public static List<MyReservationResponse> of(List<Reservation> reservations, List<WaitingWithRank> waitings) {
        List<MyReservationResponse> reservationResponses = reservations.stream()
                .map(reservation ->
                        new MyReservationResponse(
                                reservation.getId(),
                                reservation.getTheme().getName(),
                                reservation.getDate(),
                                reservation.getTime().getStartAt(),
                                RESERVED))
                .toList();

        List<MyReservationResponse> waitingResponses = waitings.stream()
                .map(waiting ->
                        new MyReservationResponse(
                                waiting.getId(),
                                waiting.getTheme().getName(),
                                waiting.getDate(),
                                waiting.getTime().getStartAt(),
                                waiting.getRank() + WAITING))
                .toList();

        return Stream.concat(reservationResponses.stream(), waitingResponses.stream())
                .collect(Collectors.toList());

    }


}
