package roomescape.domain.reservation;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import roomescape.domain.member.Member;
import roomescape.domain.schedule.ReservationTime;
import roomescape.domain.schedule.Schedule;
import roomescape.domain.theme.Theme;

@Entity
@Table(name = "waiting")
@EntityListeners(AuditingEntityListener.class)
public class ReservationWaiting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Member member;

    @ManyToOne
    private Theme theme;

    @Embedded
    private Schedule schedule;

    @CreatedDate
    private LocalDateTime createdAt;

    protected ReservationWaiting() {
    }

    public ReservationWaiting(Member member, Theme theme, Schedule schedule) {
        this.member = member;
        this.theme = theme;
        this.schedule = schedule;
    }

    public boolean isRegisteredBefore(LocalDateTime dateTime) {
        return createdAt.isBefore(dateTime);
    }

    public Long getId() {
        return id;
    }

    public Member getMember() {
        return member;
    }

    public String getMemberName() {
        return member.getMemberName().getValue();
    }

    public Theme getTheme() {
        return theme;
    }

    public Schedule getSchedule() {
        return schedule;
    }

    public LocalDate getDate() {
        return schedule.getDate();
    }

    public ReservationTime getReservationTime() {
        return schedule.getReservationTime();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
