package roomescape.reservation.model;

import roomescape.global.exception.AlreadyEntityException;
import roomescape.member.model.Member;

import java.time.LocalDate;

public class Reservation {

    private final Long id;
    private final Member member;
    private final LocalDate date;
    private final ReservationTime time;
    private final Theme theme;

    public Reservation(Long id, Member member, LocalDate date, ReservationTime time, Theme theme) {
        this.id = id;
        this.member = member;
        this.date = date;
        this.time = time;
        this.theme = theme;
    }

    public Reservation toEntity(Long id) {
        if (this.id == null) {
            return new Reservation(id, member, date, time, theme);
        }
        throw new AlreadyEntityException("해당 예약은 이미 엔티티화 된 상태입니다.");
    }

    public Long getId() {
        return id;
    }

    public Member getMember() {
        return member;
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
}
