package roomescape.domain.reservation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import roomescape.domain.member.Member;
import roomescape.exception.reservation.DateTimePassedException;

@Entity
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theme_id")
    private Theme theme;
    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_time_id")
    private ReservationTime time;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ReservationStatus status;

    public Reservation(Member member, Theme theme, LocalDate date, ReservationTime time, ReservationStatus status) {
        validateDateTimeHasPassed(date, time);
        this.member = member;
        this.theme = theme;
        this.date = date;
        this.time = time;
        this.status = status;
    }

    protected Reservation() {
    }

    private void validateDateTimeHasPassed(LocalDate date, ReservationTime time) {
        if (LocalDateTime.of(date, time.getStartAt()).isBefore(LocalDateTime.now())) {
            throw new DateTimePassedException();
        }
    }

    public void updateStatus(ReservationStatus reservationStatus) {
        this.status = reservationStatus;
    }

    public Long getId() {
        return id;
    }

    public Member getMember() {
        return member;
    }

    public String memberName() {
        return member.getName();
    }

    public Theme getTheme() {
        return theme;
    }

    public LocalDate getDate() {
        return date;
    }

    public ReservationTime getTime() {
        return time;
    }

    public LocalTime getStartAt() {
        return time.getStartAt();
    }

    public String memberEmail() {
        return member.getEmail();
    }

    public String themeName() {
        return theme.getName();
    }

    public Long themeId() {
        return theme.getId();
    }

    public ReservationStatus getReservationStatus() {
        return status;
    }
}
