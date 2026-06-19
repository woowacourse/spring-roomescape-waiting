package roomescape.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.Objects;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class Reservation {

    private final Long id;
    private final PersonName name;
    private final ReservationDate date;
    private final ReservationTime time;
    private final Theme theme;
    private final ReservationStatus status;

    private static void validateId(final Long id) {
        if (id == null) {
            throw new IllegalArgumentException("예약 ID는 비워둘 수 없습니다.");
        }
    }

    public static Reservation prepare(final String name, final LocalDate date, final ReservationTime time, final Theme theme) {
        return new Reservation(
                null,
                new PersonName(name),
                new ReservationDate(date),
                time,
                theme,
                ReservationStatus.PENDING
        );
    }

    public static Reservation from(
            final Long id,
            final String name,
            final LocalDate date,
            final ReservationTime time,
            final Theme theme,
            final ReservationStatus status
    ) {
        validateId(id);
        return new Reservation(
                id,
                new PersonName(name),
                new ReservationDate(date),
                time,
                theme,
                status
        );
    }

    public Reservation withId(final Long id) {
        validateId(id);
        return new Reservation(
                id,
                this.name,
                this.date,
                this.time,
                this.theme,
                this.status
        );
    }

    public Reservation modifyDateAndTime(final LocalDate newDate, final ReservationTime newReservationTime) {
        return new Reservation(
                this.id,
                this.name,
                new ReservationDate(newDate),
                Objects.requireNonNullElse(newReservationTime, this.time),
                this.theme,
                this.status
        );
    }

    public boolean isOwner(final String name) {
        return this.name.isSameName(name);
    }

    public String getName() {
        return this.name.name();
    }

    public LocalDate getDate() {
        return this.date.date();
    }
}
