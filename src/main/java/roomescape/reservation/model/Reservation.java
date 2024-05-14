package roomescape.reservation.model;

import roomescape.member.model.Member;

import java.time.LocalDate;

public class Reservation {

    private final Long id;
    private final ReservationDate date;
    private final ReservationTime time;
    private final Theme theme;
    private final Member member;

    public static Reservation of(
            final LocalDate date,
            final ReservationTime time,
            final Theme theme,
            final Member member
    ) {
        checkNull(time, theme, member);

        final ReservationDate reservationDate = new ReservationDate(date);
        return new Reservation(
                null,
                reservationDate,
                time,
                theme,
                member
        );
    }

    private static void checkNull(
            final ReservationTime reservationTime,
            final Theme theme,
            final Member member
    ) {
        if (reservationTime == null || theme == null || member == null) {
            throw new IllegalArgumentException("시간, 테마, 회원 정보는 Null을 입력할 수 없습니다.");
        }
    }

    public static Reservation of(
            final Long id,
            final LocalDate date,
            final ReservationTime time,
            final Theme theme,
            final Member member
    ) {
        checkNull(time, theme, member);

        return new Reservation(
                id,
                new ReservationDate(date),
                time,
                theme,
                member
        );
    }

    private Reservation(
            final Long id,
            final ReservationDate date,
            final ReservationTime time,
            final Theme theme,
            final Member member
    ) {
        this.id = id;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.member = member;
    }

    public Reservation initializeIndex(final Long reservationId) {
        return new Reservation(reservationId, date, time, theme, member);
    }

    public Long getId() {
        return id;
    }

    public ReservationDate getDate() {
        return date;
    }

    public ReservationTime getTime() {
        return time;
    }

    public Theme getTheme() {
        return theme;
    }

    public Member getMember() {
        return member;
    }
}
