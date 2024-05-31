package roomescape.reservation.domain;

import java.time.LocalDate;

import roomescape.member.domain.Member;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.Time;

public class ReservationWaitingBuilder {
    private Long id;
    private Member member;
    private ReservationDetail reservationDetail;
    private Time time;
    private Theme theme;
    private LocalDate date;

    public ReservationWaitingBuilder() {
    }

    public ReservationWaitingBuilder id(Long id) {
        this.id = id;
        return this;
    }

    public ReservationWaitingBuilder member(Member member) {
        this.member = member;
        return this;
    }

    public ReservationWaitingBuilder reservationDetail(ReservationDetail reservationDetail) {
        this.reservationDetail = reservationDetail;
        return this;
    }

    public ReservationWaitingBuilder time(Time time) {
        this.time = time;
        return this;
    }

    public ReservationWaitingBuilder theme(Theme theme) {
        this.theme = theme;
        return this;
    }

    public ReservationWaitingBuilder date(LocalDate date) {
        this.date = date;
        return this;
    }

    public ReservationWaiting build() {
        if (reservationDetail == null) {
            return new ReservationWaiting(id, member, new ReservationDetail(theme, time, date));
        }
        return new ReservationWaiting(id, member, reservationDetail);
    }
}
