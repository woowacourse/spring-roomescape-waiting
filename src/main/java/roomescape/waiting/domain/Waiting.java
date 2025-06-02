package roomescape.waiting.domain;

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
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationSchedule;
import roomescape.reservation.domain.ReservationTime;
import roomescape.theme.domain.Theme;

@Entity
public class Waiting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Member member;

    @Embedded
    private ReservationSchedule reservationSchedule;

    protected Waiting() {}

    public Waiting(
        final Long id,
        final Member member,
        final ReservationSchedule reservationSchedule
    ) {
        this.id = id;
        this.member = member;
        this.reservationSchedule = reservationSchedule;
    }

    public Waiting(final Member member, final ReservationSchedule reservationSchedule) {
        this(null, member, reservationSchedule);
    }

    public Reservation convertToReservation() {
        return new Reservation(
            this.reservationSchedule, this.member);
    }

    public Long getId() {
        return id;
    }

    public ReservationSchedule getReservationSchedule() {
        return reservationSchedule;
    }

    public Member getMember() {
        return member;
    }

    public boolean isSameMemberId(Long memberId) {
        return this.member.isSameMember(memberId);
    }

    public ReservationTime getReservationTime() {
        return reservationSchedule.getReservationTime();
    }

    public Theme getTheme() {
        return reservationSchedule.getTheme();
    }

    public LocalDate getDate() {
        return reservationSchedule.getDate();
    }

    public String getMemberName() {
        return member.getNameValue();
    }

    public String getThemeName() {
        return reservationSchedule.getThemeName();
    }

    public LocalTime getReservationStartAt() {
        return reservationSchedule.getStartAt();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Waiting waiting = (Waiting) o;
        return Objects.equals(id, waiting.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
