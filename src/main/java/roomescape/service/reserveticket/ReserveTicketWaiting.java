package roomescape.service.reserveticket;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.domain.reservation.ReservationStatus;

public class ReserveTicketWaiting {

    private final Long id;
    private final String name;
    private final LocalDate date;
    private final LocalTime startAt;
    private final ReservationStatus reservationStatus;
    private final int waitNumber;
    private final String themeName;
    private final Long memberId;

    public ReserveTicketWaiting(Long id, String name, LocalDate date, LocalTime startAt,
                                ReservationStatus reservationStatus, int waitNumber, String themeName, Long memberId) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.startAt = startAt;
        this.reservationStatus = reservationStatus;
        this.waitNumber = waitNumber;
        this.themeName = themeName;
        this.memberId = memberId;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalTime getStartAt() {
        return startAt;
    }

    public ReservationStatus getReservationStatus() {
        return reservationStatus;
    }

    public int getWaitRank() {
        return waitNumber;
    }

    public String getThemeName() {
        return themeName;
    }

    public boolean isSameMember(Long memberId) {
        return this.memberId == memberId;
    }
}
