package roomescape.reservation.domain;

import java.time.LocalDate;

import roomescape.member.domain.Member;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.Time;

public class ReservationBuilder {
    private Long id;
    private Member member;
    private ReservationDetail reservationDetail;
    private Time time;
    private Theme theme;
    private LocalDate date;

    public ReservationBuilder() {
    }

    public ReservationBuilder id(Long id) {
        this.id = id;
        return this;
    }

    public ReservationBuilder member(Member member) {
        this.member = member;
        return this;
    }

    public ReservationBuilder reservationDetail(ReservationDetail reservationDetail) {
        this.reservationDetail = reservationDetail;
        return this;
    }

    public ReservationBuilder time(Time time) {
        this.time = time;
        return this;
    }

    public ReservationBuilder theme(Theme theme) {
        this.theme = theme;
        return this;
    }

    public ReservationBuilder date(LocalDate date) {
        this.date = date;
        return this;
    }

    public Reservation build() {
        if (reservationDetail == null) {
            return new Reservation(id, member, new ReservationDetail(theme, time, date));
        }
        return new Reservation(id, member, reservationDetail);
    }
}
