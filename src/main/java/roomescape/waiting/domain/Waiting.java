package roomescape.waiting.domain;

import jakarta.persistence.*;
import roomescape.exception.DomainValidationException;
import roomescape.member.domain.Member;
import roomescape.schedule.domain.Schedule;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "waiting")
public class Waiting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "schedule_id", nullable = false)
    private Schedule schedule;

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    protected Waiting() {
    }

    public Waiting(Long id, Schedule schedule, Member member) {
        validate(schedule, member);
        this.id = id;
        this.schedule = schedule;
        this.member = member;
    }

    public static Waiting generateWithPrimaryKey(Waiting waiting, Long newPrimaryKey) {
        return new Waiting(newPrimaryKey, waiting.schedule, waiting.member);
    }

    private void validate(Schedule schedule, Member member) {
        if (schedule == null || member == null) {
            throw new DomainValidationException("대기 정보가 비어있습니다.");
        }
    }

    public LocalDateTime getScheduleDateTime() {
        return schedule.getDateTime();
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
        Waiting that = (Waiting) o;
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
