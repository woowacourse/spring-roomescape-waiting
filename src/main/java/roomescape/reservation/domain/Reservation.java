package roomescape.reservation.domain;

import roomescape.time.domain.ReservationTime;
import roomescape.theme.domain.Theme;
import roomescape.exception.BadRequestException;
import roomescape.member.domain.Member;

import java.time.LocalDate;

public class Reservation {

    private final Long id;
    private final Member member;
    private final LocalDate date;
    private final ReservationTime time;
    private final Theme theme;

    public Reservation(Long id, Member member, LocalDate date, ReservationTime time, Theme theme) {
        validate(member, date, time, theme);
        this.id = id;
        this.member = member;
        this.date = date;
        this.time = time;
        this.theme = theme;
    }

    private void validate(Member member, LocalDate date, ReservationTime time, Theme theme) {
        validateNullField(member, date, time, theme);
        validateName(member.getName());
    }

    private void validateNullField(Member member, LocalDate date, ReservationTime time, Theme theme) {
        if (member == null || date == null || time == null || theme == null) {
            throw new IllegalArgumentException("예약 필드에는 빈 값이 들어올 수 없습니다.");
        }
    }

    private void validateName(String name) {
        if (name.isBlank()) {
            throw new IllegalArgumentException("이름은 공백일 수 없습니다.");
        }
    }

    public Reservation(Member member, LocalDate date, ReservationTime reservationTime, Theme theme) {
        this(null, member, date, reservationTime, theme);
    }

    public Reservation(Long id, Reservation reservation) {
        this(id, reservation.member, reservation.date, reservation.time, reservation.theme);
    }

    public boolean isSameMember(Reservation other) {
        return member.isSameMember(other.member);
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
