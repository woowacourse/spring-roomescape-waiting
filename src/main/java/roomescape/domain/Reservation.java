package roomescape.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class Reservation {

    private final long id;
    private final String name;
    private final LocalDate date;
    private final ReservationTime time;
    private final ReservationTheme theme;

    private Reservation(final long id, final String name, final LocalDate date, final ReservationTime time,
                        final ReservationTheme theme) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.time = time;
        this.theme = theme;
    }

    public Reservation(final long id, final String name, final String date, final ReservationTime time,
                       final ReservationTheme theme) {
        this(id, name, LocalDate.parse(date), time, theme);
    }

    public Reservation(final String name, final LocalDate date, final ReservationTime time,
                       final ReservationTheme theme) {
        validateFutureDateTime(date, time.getStartAt());
        this.id = 0L;
        this.name = name;
        this.date = date;
        this.time = time;
        this.theme = theme;
    }

    public Reservation toEntity(long id) {
        return new Reservation(id, name, date, time, theme);
    }

    public boolean isDuplicateReservation(Reservation reservation) {
        return this.date.equals(reservation.date) && this.time.isSameTime(reservation.time);
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public LocalDate getDate() {
        return date;
    }

    public ReservationTime getTime() {
        return time;
    }

    public ReservationTheme getTheme() {
        return theme;
    }

    private void validateFutureDateTime(final LocalDate date, final LocalTime time) {
        final LocalDateTime reservationDateTime = LocalDateTime.of(date, time);
        final LocalDateTime now = LocalDateTime.now();
        if ( reservationDateTime.isBefore(now) || reservationDateTime.isEqual(now)) {
            throw new IllegalArgumentException("[ERROR] 이전 시각으로 예약할 수 없습니다.");
        }
    }
}
