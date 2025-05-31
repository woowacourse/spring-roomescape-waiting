package roomescape.reservation.domain;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberName;
import roomescape.theme.domain.Theme;

@Entity
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private ReservationSchedule reservationSchedule;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    protected Reservation() {

    }

    public Reservation(
        final Long id,
        final ReservationSchedule reservationSchedule,
        final Member member
    ) {
        this.id = id;
        this.reservationSchedule = reservationSchedule;
        this.member = member;
    }

    public Reservation(final ReservationSchedule reservationSchedule, final Member member) {
        this(null, reservationSchedule, member);
    }

    public boolean hasConflictWith(final ReservationTime reservationTime, final Theme theme) {
        final LocalTime startAt = this.reservationSchedule.getStartAt();
        return reservationTime.hasConflict(theme.getDuration(), startAt);
    }

    public Long getId() {
        return id;
    }

    public MemberName getName() {
        return member.getName();
    }

    public LocalDate getDate() {
        return reservationSchedule.getDate();
    }

    public ReservationTime getTime() {
        return reservationSchedule.getReservationTime();
    }

    public Theme getTheme() {
        return reservationSchedule.getTheme();
    }

    public Member getMember() {
        return member;
    }

    public String getThemeName() {
        return reservationSchedule.getThemeName();
    }

    public LocalTime getStartAt() {
        return reservationSchedule.getStartAt();
    }


    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Reservation that = (Reservation) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
