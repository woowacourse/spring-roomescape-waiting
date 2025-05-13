package roomescape.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import roomescape.exception.ArgumentNullException;
import roomescape.exception.PastDateTimeReservationException;

public class Reservation {

    private final Long id;
    private final Member member;
    private final LocalDate date;
    private final ReservationTime reservationTime;
    private final Theme theme;

    private Reservation(final Long id, final Member member, final LocalDate date, final ReservationTime reservationTime,
                        final Theme theme) {
        validateNull(member, date, reservationTime, theme);
        this.id = id;
        this.member = member;
        this.date = date;
        this.reservationTime = reservationTime;
        this.theme = theme;
    }

    public static Reservation of(final Long id, final Member member, final LocalDate date,
                                 final ReservationTime reservationTime, final Theme theme) {
        return new Reservation(id, member, date, reservationTime, theme);
    }

    public static Reservation createWithoutId(final Member member, final LocalDate date,
                                              final ReservationTime reservationTime, final Theme theme) {
        return new Reservation(null, member, date, reservationTime, theme);
    }

    private static void validateNull(Member member, LocalDate date, ReservationTime reservationTime, Theme theme) {
        if (member == null) {
            throw new ArgumentNullException("member");
        }
        if (date == null) {
            throw new ArgumentNullException("date");
        }
        if (reservationTime == null) {
            throw new ArgumentNullException("reservationTime");
        }
        if (theme == null) {
            throw new ArgumentNullException("theme");
        }
    }

    public void validateDateTime() {
        LocalDateTime dateTime = LocalDateTime.of(date, reservationTime.getStartAt());
        if (LocalDateTime.now().isAfter(dateTime)) {
            throw new PastDateTimeReservationException();
        }
    }

    public Reservation withId(Long id) {
        return new Reservation(id, member, date, reservationTime, theme);
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

    public ReservationTime getReservationTime() {
        return reservationTime;
    }

    public Theme getTheme() {
        return theme;
    }
}
