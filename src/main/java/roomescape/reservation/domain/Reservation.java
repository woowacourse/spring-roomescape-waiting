package roomescape.reservation.domain;

import jakarta.persistence.*;
import roomescape.exception.DomainValidationException;
import roomescape.member.domain.Member;
import roomescape.schedule.domain.Schedule;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "reservation")
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "schedule_id", nullable = false)
    private Schedule schedule;

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    protected Reservation() {
    }

    public Reservation(Long id, Schedule schedule, Member member) {
        validate(schedule, member);
        this.id = id;
        this.schedule = schedule;
        this.member = member;
    }

    public static Reservation generateWithPrimaryKey(Reservation reservation, Long newPrimaryKey) {
        return new Reservation(newPrimaryKey, reservation.schedule, reservation.member);
    }

    private void validate(Schedule schedule, Member member) {
        if (schedule == null || member == null) {
            throw new DomainValidationException("예약 정보가 비어있습니다.");
        }
    }

    public LocalDateTime getReservationDateTime() {
        return schedule.getDateTime();
    }

    public ReservationStatus getStatus() {
        // todo: 스케쥴한테 상태를 물어도 괜찮을까?
        LocalDateTime reservationDateTime = getReservationDateTime();
        if (reservationDateTime.isBefore(LocalDateTime.now())) {
            return ReservationStatus.COMPLETED;
        }
        return ReservationStatus.RESERVED;
    }

    public Long getId() {
        return id;
    }

    public Schedule getSchedule() {
        return schedule;
    }

    public Member getMember() {
        return member;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Reservation that = (Reservation) o;
        if (id == null && that.id == null) {
            return false;
        }
        return Objects.equals(getId(), that.getId());

    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
