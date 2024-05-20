package roomescape.domain.reservation;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.domain.member.Member;
import roomescape.domain.schedule.ReservationTime;
import roomescape.domain.schedule.Schedule;
import roomescape.domain.theme.Theme;

@Entity
@Table(name = "reservation")
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Member member;

    @Embedded
    @AttributeOverride(name = "date.value", column = @Column(name = "DATE"))
    private Schedule schedule;

    @ManyToOne
    private Theme theme;

    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    protected Reservation() {
    }

    public Reservation(Member member, Schedule schedule, Theme theme, ReservationStatus status) {
        this.member = member;
        this.schedule = schedule;
        this.theme = theme;
        this.status = status;
    }

    public boolean isReservationOf(Long memberId) {
        return memberId.equals(member.getId());
    }

    public Long getId() {
        return id;
    }

    public Member getMember() {
        return member;
    }

    public LocalDate getDate() {
        return schedule.getDate();
    }

    public LocalTime getTime() {
        return schedule.getTime();
    }

    public ReservationTime getReservationTime() {
        return schedule.getReservationTime();
    }

    public Theme getTheme() {
        return theme;
    }

    public ReservationStatus getStatus() {
        return status;
    }
}
