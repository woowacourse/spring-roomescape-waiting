package roomescape.reservation.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.time.LocalDateTime;
import roomescape.member.domain.Member;

@Entity
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDate date;
    private LocalDateTime createdAt;
    @Enumerated(value = EnumType.STRING)
    private Status status;
    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;
    @ManyToOne(fetch = FetchType.LAZY)
    private Theme theme;
    @ManyToOne(fetch = FetchType.LAZY)
    private ReservationTime reservationTime;

    public Reservation() {
    }

    public Reservation(Member member, LocalDate date, Theme theme, ReservationTime reservationTime, Status status) {
        validateLastDate(date);
        this.member = member;
        this.date = date;
        this.theme = theme;
        this.reservationTime = reservationTime;
        this.createdAt = LocalDateTime.now();
        this.status = status;
    }

    public Reservation(Long id, Member member, LocalDate date, Theme theme, ReservationTime reservationTime,
                       Status status) {
        this.id = id;
        this.member = member;
        this.date = date;
        this.theme = theme;
        this.reservationTime = reservationTime;
        this.createdAt = LocalDateTime.now();
        this.status = status;
    }

    public Reservation(Long id, LocalDate date, Status status, Member member, Theme theme,
                       ReservationTime reservationTime, LocalDateTime createdAt) {
        this.id = id;
        this.date = date;
        this.status = status;
        this.member = member;
        this.theme = theme;
        this.reservationTime = reservationTime;
        this.createdAt = createdAt;
    }

    private void validateLastDate(LocalDate date) {
        if (date.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("지난 날짜는 예약할 수 없습니다.");
        }
    }

    public Long getId() {
        return id;
    }

    public Member getMember() {
        return member;
    }

    public Theme getTheme() {
        return theme;
    }

    public LocalDate getDate() {
        return date;
    }

    public ReservationTime getTime() {
        return reservationTime;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Status getStatus() {
        return status;
    }
}
