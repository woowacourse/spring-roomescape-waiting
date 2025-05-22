package roomescape.presentation.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.business.domain.Reservation;
import roomescape.business.domain.WaitingWithRank;

public record ReservationMineResponse(Long id, String theme, LocalDate date, LocalTime time, String status) {

    public static ReservationMineResponse from(final Reservation reservation) {
        return new ReservationMineResponse(reservation.getId(), reservation.getTheme().getName(), reservation.getDate(),
                reservation.getTime().getStartAt(), "예약");
    }

    public static ReservationMineResponse from(final WaitingWithRank waitingWithRank) {
        return new ReservationMineResponse(waitingWithRank.waiting().getId(),
                waitingWithRank.waiting().getTheme().getName(), waitingWithRank.waiting().getDate(),
                waitingWithRank.waiting().getTime().getStartAt(), waitingWithRank.rank() + "번째 예약 대기");
    }
}
