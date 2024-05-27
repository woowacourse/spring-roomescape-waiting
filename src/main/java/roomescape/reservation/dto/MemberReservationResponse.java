package roomescape.reservation.dto;

import roomescape.reservation.domain.Reservation;
import java.time.LocalDate;
import java.time.LocalTime;

public class MemberReservationResponse {

    private final Long reservationId;
    private final String theme;
    private final LocalDate date;
    private final LocalTime time;
    private final String status;
    private final Long sequence;

    public MemberReservationResponse(Long reservationId, String theme, LocalDate date, LocalTime time, String status, Long sequence) {
        this.reservationId = reservationId;
        this.theme = theme;
        this.date = date;
        this.time = time;
        this.status = status;
        this.sequence = sequence;
    }

    public MemberReservationResponse(Reservation reservation, Long sequence) {
        this(reservation.getId(),
                reservation.getTheme().getName(),
                reservation.getDate(),
                reservation.getTime().getStartAt(),
                reservation.getStatus().getStatusName(),
                sequence
        );
    }

    public Long getReservationId() {
        return reservationId;
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

    public Long getSequence() {
        return sequence;
    }
}
