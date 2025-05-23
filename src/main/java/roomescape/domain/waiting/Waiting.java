package roomescape.domain.waiting;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import roomescape.domain.member.Member;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.exception.reservation.InvalidReservationException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
public class Waiting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private LocalDate date;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private ReservationTime time;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Theme theme;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Member member;

    public Waiting() {

    }

    public Waiting(Long id, LocalDate date, ReservationTime time, Theme theme, Member member) {
        validate(date, time, member);
        this.id = id;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.member = member;
    }

    private void validate(LocalDate date, ReservationTime time, Member member) {
        if (date == null || time == null) {
            throw new InvalidReservationException("시간은 공백일 수 없습니다.");
        }
        if (theme == null) {
            throw new InvalidReservationException("테마는 공백일 수 없습니다.");
        }
        if (member == null) {
            throw new InvalidReservationException("멤버는 공백일 수 없습니다.");
        }
    }

    public boolean isBeforeDateTime(LocalDateTime compareDateTime) {
        LocalDateTime reservationDateTime = LocalDateTime.of(date, getStartAt());
        return reservationDateTime.isBefore(compareDateTime);
    }

    public Long getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public ReservationTime getTime() {
        return time;
    }

    public Theme getTheme() {
        return theme;
    }

    public Member getMember() {
        return member;
    }

    public LocalTime getStartAt() {
        return time.getTime();
    }

    public String getThemeName() {
        return theme.getName();
    }

    public String getName() {
        return member.getName();
    }
}
