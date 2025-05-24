package roomescape.domain.reservation;

import static org.hibernate.annotations.GenerationTime.INSERT;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Objects;
import org.hibernate.annotations.Generated;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.schdule.ReservationSchedule;

@Entity
@Table(name = "reservation_wait")
public class ReservationWait {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @OneToOne
    @JoinColumn(name = "schedule_id")
    private ReservationSchedule schedule;

    @Generated(INSERT)
    @Column(name = "created_at", updatable = false, insertable = false)
    private LocalDateTime createdAt;

    protected ReservationWait() {
    }

    public ReservationWait(
            final Long id,
            final Member member,
            final ReservationSchedule schedule
    ) {
        this.id = id;
        this.member = Objects.requireNonNull(member);
        this.schedule = Objects.requireNonNull(schedule);
    }

    public Long getId() {
        return id;
    }

    public Member getMember() {
        return member;
    }

    public ReservationSchedule getSchedule() {
        return schedule;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Long calculateRank() {
        return schedule.getWaits().stream()
                .filter(wait -> wait.getCreatedAt().isBefore(this.createdAt))
                .count() + 1;
    }
}
