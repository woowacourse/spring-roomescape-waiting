package roomescape.reservation.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.dto.WaitingWithRank;

public class MyReservationResponse {

    private static final long RESERVATION_STATUS = 0;

    private final Long id;
    private final String theme;
    private final LocalDate date;
    @JsonFormat(pattern = "HH:mm")
    private final LocalTime time;
    private final Long status;

    public MyReservationResponse(final Long id, final LocalDate date, final LocalTime time, final String theme,
            final Long status) {
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
                RESERVATION_STATUS
        );
    }

    public MyReservationResponse(final WaitingWithRank waitingWithRank) {
        this(
                waitingWithRank.waiting().getId(),
                waitingWithRank.waiting().getDate(),
                waitingWithRank.waiting().getReservationTime().getStartAt(),
                waitingWithRank.waiting().getTheme().getName(),
                waitingWithRank.rank()
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

    public Long getStatus() {
        return status;
    }
}
