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

    private void validate(Schedule schedule, Member member) {
        if (schedule == null || member == null) {
            throw new DomainValidationException("예약 정보가 비어있습니다.");
        }
    }

    public ReservationStatus getStatus() {
        if (schedule.isBefore(LocalDateTime.now())) {
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
        if (!(o instanceof Reservation that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
