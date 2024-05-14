package roomescape.reservation.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.member.domain.Member;

@Entity
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theme_id")
    private Theme theme;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "time_id")
    private ReservationTime reservationTime;

    public Reservation() {
    }

    public Reservation(Member member, LocalDate date, Theme theme, ReservationTime reservationTime) {
        validateLastDate(date);
        this.member = member;
        this.date = date;
        this.theme = theme;
        this.reservationTime = reservationTime;
    }

    public Reservation(Long id, Member member, LocalDate date, Theme theme, ReservationTime reservationTime) {
        this.id = id;
        this.member = member;
        this.date = date;
        this.theme = theme;
        this.reservationTime = reservationTime;
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

    public LocalTime getStartAt() {
        return reservationTime.getStartAt();
    }
}
