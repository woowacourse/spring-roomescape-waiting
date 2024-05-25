package roomescape.domain.reservation;

import jakarta.persistence.*;
import roomescape.domain.member.Member;

import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table
public class Reservation {
    public static int RESERVATION_RANK = 0;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    @Embedded
    private ReservationSlot reservationSlot;

    public Reservation(Member member, ReservationSlot reservationSlot) {
        this.member = member;
        this.reservationSlot = reservationSlot;
    }

    protected Reservation() {
    }

    public Long getId() {
        return id;
    }

    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }

    public LocalDate getDate() {
        return reservationSlot.getDate();
    }

    public ReservationTime getTime() {
        return reservationSlot.getTime();
    }

    public Theme getTheme() {
        return reservationSlot.getTheme();
    }

    public ReservationSlot getReservationSlot() {
        return reservationSlot;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !(o instanceof Reservation)) {
            return false;
        }
        Reservation that = (Reservation) o;
        return Objects.equals(this.getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
