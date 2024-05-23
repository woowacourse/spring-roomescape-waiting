package roomescape.reservation.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.vo.WaitingWithRank;

public class MyReservationResponse {

    private final Long id;
    private final String theme;
    private final LocalDate date;
    private final LocalTime time;
    private final String status;

    public MyReservationResponse(final Long id, final LocalDate date, final LocalTime time, final String theme,
            final String status) {
        this.id = id;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.status = status;
    }

    public MyReservationResponse(final Reservation reservation) {
        this(
                reservation.getId(),
                reservation.getDate(),
                reservation.getTime().getStartAt(),
                reservation.getTheme().getName(),
                ReservationStatus.RESERVATION.getStatus()
        );
    }

    public MyReservationResponse(final WaitingWithRank waitingWithRank) {
        this(
                waitingWithRank.waiting().getId(),
                waitingWithRank.waiting().getDate(),
                waitingWithRank.waiting().getReservationTime().getStartAt(),
                waitingWithRank.waiting().getTheme().getName(),
                ReservationStatus.WAITING.formatRankWithSuffix(waitingWithRank.rank())
        );
    }

    public Long getId() {
        return id;
    }

    public String getTheme() {
        return theme;
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalTime getTime() {
        return time;
    }

    public String getStatus() {
        return status;
    }
}
