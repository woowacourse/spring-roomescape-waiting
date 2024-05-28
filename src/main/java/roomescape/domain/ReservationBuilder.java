package roomescape.domain;

import java.time.LocalDate;

public class ReservationBuilder {

    private Long id;
    private Member member;
    private LocalDate date;
    private ReservationTime time;
    private Theme theme;
    private ReservationStatus status;

    public ReservationBuilder id(Long id) {
        this.id = id;
        return this;
    }

    public ReservationBuilder member(Member member) {
        this.member = member;
        return this;
    }

    public ReservationBuilder date(LocalDate date) {
        this.date = date;
        return this;
    }

    public ReservationBuilder time(ReservationTime time) {
        this.time = time;
        return this;
    }

    public ReservationBuilder theme(Theme theme) {
        this.theme = theme;
        return this;
    }

    public ReservationBuilder status(ReservationStatus status) {
        this.status = status;
        return this;
    }

    public Reservation build() {
        return new Reservation(id, member, date, time, theme, status);
    }
}
