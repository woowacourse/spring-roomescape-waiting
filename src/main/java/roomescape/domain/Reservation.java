package roomescape.domain;

import java.time.LocalDate;
import roomescape.exception.BadRequestException;

public class Reservation {
    private final Long id;
    private final Member member;
    private final LocalDate date;
    private final ReservationTime time;
    private final RoomTheme theme;

    public Reservation(Member member, LocalDate date, ReservationTime time, RoomTheme theme) {
        this(null, member, date, time, theme);
    }

    public Reservation(Long id, Member member, LocalDate date, ReservationTime time, RoomTheme theme) {
        validateMember(member);
        validateDate(date);
        validateReservationTime(time);
        validateRoomTheme(theme);
        this.id = id;
        this.member = member;
        this.date = date;
        this.time = time;
        this.theme = theme;
    }

    private void validateRoomTheme(RoomTheme theme) {
        if (theme == null) {
            throw new BadRequestException("테마에 빈값을 입력할 수 없습니다.");
        }
    }

    private void validateReservationTime(ReservationTime time) {
        if (time == null) {
            throw new BadRequestException("시간에 빈값을 입력할 수 없습니다.");
        }
    }

    private void validateDate(LocalDate date) {
        if (date == null) {
            throw new BadRequestException("날짜에 빈값을 입력할 수 없습니다.");
        }
    }

    private void validateMember(Member member) {
        if (member == null) {
            throw new BadRequestException("사용자에 빈값을 입력할 수 없습니다.");
        }
    }

    public Reservation setId(Long id) {
        return new Reservation(id, member, date, time, theme);
    }

    public boolean hasDateTime(LocalDate date, ReservationTime reservationTime) {
        return this.date.equals(date)
                && this.time.getStartAt().equals(reservationTime.getStartAt());
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

    public RoomTheme getTheme() {
        return theme;
    }
}
