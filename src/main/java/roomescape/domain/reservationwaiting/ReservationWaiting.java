package roomescape.domain.reservationwaiting;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;
import roomescape.domain.AuditingEntity;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"reservation_id", "member_id"}))
public class ReservationWaiting extends AuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Reservation reservation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Member member;

    protected ReservationWaiting() {
    }

    public ReservationWaiting(Reservation reservation, Member member) {
        this(null, reservation, member);
    }

    private ReservationWaiting(Long id, Reservation reservation, Member member) {
        this.id = id;
        this.reservation = reservation;
        this.member = member;
    }

    public boolean isSameMember(Member member) {
        return this.member.equals(member);
    }

    public void validateOwner(Member member) {
        if (!isSameMember(member)) {
            throw new IllegalArgumentException("예약 대기한 회원이 아닙니다.");
        }
    }

    public void validateFutureReservationWaiting(LocalDateTime now) {
        reservation.validateFutureReservation(now);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ReservationWaiting reservationWaiting)) {
            return false;
        }
        return getId() != null && Objects.equals(getId(), reservationWaiting.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public Long getId() {
        return id;
    }

    public LocalDate getDate() {
        return reservation.getDate();
    }

    public String getThemeName() {
        return reservation.getTheme().getRawName();
    }

    public LocalTime getTime() {
        return reservation.getTime().getStartAt();
    }

    public Reservation getReservation() {
        return reservation;
    }

    public Member getMember() {
        return member;
    }
}
