package roomescape.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import roomescape.domain.ReservationStatus;
import roomescape.domain.Reservations;
import roomescape.domain.Waiting;
import roomescape.entity.Reservation;

public record ReservationDetailResponse(
        long reservationId,
        String theme,
        LocalDate date,
        LocalTime time,
        String status
) {
    public static ReservationDetailResponse from(Reservation reservation) {
        return new ReservationDetailResponse(
                reservation.getId(),
                reservation.getTheme().getName(),
                reservation.getDate(),
                reservation.getReservationTime().getStartAt(),
                getStatusName(reservation.getStatus()));
    }

    public static ReservationDetailResponse from(Waiting waiting) {
        Reservation reservation = waiting.reservation();
        return new ReservationDetailResponse(
                reservation.getId(),
                reservation.getTheme().getName(),
                reservation.getDate(),
                reservation.getReservationTime().getStartAt(),
                String.format(getStatusName(reservation.getStatus()), waiting.rank()));
    }

    public static List<ReservationDetailResponse> of(Reservations reservations, List<Waiting> waitings) {
        List<ReservationDetailResponse> responses = new ArrayList<>();
        for(Reservation reservation : reservations.getReservations()){
            ReservationDetailResponse response = waitings.stream()
                    .filter(waiting -> waiting.reservation().equals(reservation))
                    .findFirst()
                    .map(ReservationDetailResponse::from)
                    .orElse(from(reservation));
            responses.add(response);
        }
        return responses;
    }

    private static String getStatusName(ReservationStatus status) {
        return switch (status) {
            case BOOKED -> "예약";
            case WAITING -> "%d번째 예약대기";
        };
    }
}
