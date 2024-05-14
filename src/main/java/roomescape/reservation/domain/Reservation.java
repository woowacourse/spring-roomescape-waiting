package roomescape.reservation.domain;

import java.time.LocalDate;
import roomescape.member.domain.Member;

public class Reservation {

    private Long id;
    private final Member member;
    private final LocalDate date;
    private final Theme theme;
    private final ReservationTime reservationTime;

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
}
