package roomescape.domain.reservationmember;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;

public class ReservationMember {

    private final Reservation reservation;
    private final Member member;

    public ReservationMember(Reservation reservation, Member member) {
        this.reservation = reservation;
        this.member = member;
    }

    public Long getReservationId() {
        return reservation.getId();
    }

    public String getThemeName() {
        return reservation.getThemeName();
    }

    public LocalDate getDate() {
        return reservation.getDate();
    }

    public LocalTime getStartAt() {
        return reservation.getStartAt();
    }

    public String getName() {
        return member.getName();
    }

    public String getStatus() {
        return reservation.getReservationStatus();
    }
}
