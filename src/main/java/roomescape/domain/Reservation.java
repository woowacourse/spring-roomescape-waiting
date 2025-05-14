package roomescape.domain;

import roomescape.domain.member.Member;
import roomescape.exception.InvalidRequestException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

public class Reservation {

    private static final int MAX_NAME_LENGTH = 255;

    private final Long id;
    private final Member member;
    private final LocalDate date;
    private final ReservationTime time;
    private final Theme theme;

    public Reservation(
            final Long id,
            final Member member,
            final LocalDate date,
            final ReservationTime time,
            final Theme theme) {
        validateDate(date);
        validateReservationTime(time);
        validateTheme(theme);

        this.id = id;
        this.member = member;
        this.date = date;
        this.time = time;
        this.theme = theme;
    }

    public static Reservation createWithoutId(Member member, LocalDate date, ReservationTime time, Theme theme) {
        return new Reservation(null, member, date, time, theme);
    }

    public static void validateReservableTime(final LocalDate date, final LocalTime startAt) {
        LocalDateTime dateTime = LocalDateTime.of(date, startAt);
        if (dateTime.isBefore(LocalDateTime.now())) {
            throw new InvalidRequestException("[ERROR] 현 시점 이후의 날짜와 시간을 선택해주세요.");
        }
    }

    private void validateDate(final LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("[ERROR] 예약 날짜는 반드시 입력해야 합니다. 예시) YYYY-MM-DD");
        }
    }

    private void validateReservationTime(ReservationTime reservationTime) {
        if (reservationTime == null) {
            throw new IllegalArgumentException("[ERROR] 예약 시간을 반드시 입력해야 합니다.");
        }
    }

    private void validateTheme(final Theme theme) {
        if (theme == null) {
            throw new IllegalArgumentException("[ERROR] 테마는 반드시 입력해야 합니다.");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Reservation that = (Reservation) o;
        return Objects.equals(id, that.id) && Objects.equals(member, that.member) && Objects.equals(date, that.date) && Objects.equals(time, that.time) && Objects.equals(theme, that.theme);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, member, date, time, theme);
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
