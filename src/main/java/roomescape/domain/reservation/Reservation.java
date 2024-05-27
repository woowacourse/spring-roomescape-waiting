package roomescape.domain.reservation;

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
import roomescape.exception.UnauthorizedException;

@Entity
@Table(name = "reservation")
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Member member;

    @Embedded
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

    public void checkCancelAuthority(long memberId) {
        if (memberId != member.getId()) {
            throw new UnauthorizedException("예약을 삭제할 권한이 없습니다.");
        }
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

    public Schedule getSchedule() {
        return schedule;
    }
}
