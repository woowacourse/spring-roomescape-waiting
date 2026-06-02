package roomescape.domain;

import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
public class WaitingList {

    private final Long id;

    private final PersonName name;
    private final ReservationDate reservationDate;

    private final Theme theme;
    private final ReservationTime reservationTime;

    private final LocalDateTime createdAt;

    private WaitingList(final Long id, final PersonName name, final ReservationDate date, final Theme theme, final ReservationTime reservationTime, final LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.reservationDate = date;
        this.theme = theme;
        this.reservationTime = reservationTime;
        this.createdAt = createdAt;
    }

    public static WaitingList create(final String name, final LocalDate date, final Theme theme, final ReservationTime reservationTime) {
        return new WaitingList(null, new PersonName(name), new ReservationDate(date), theme, reservationTime, LocalDateTime.now());
    }

    public static WaitingList createWithId(final Long id, final String name, final LocalDate date, final Theme theme, final ReservationTime reservationTime, final LocalDateTime createdAt) {
        return new WaitingList(id, new PersonName(name), new ReservationDate(date), theme, reservationTime, createdAt);
    }

    public String getName() {
        return name.getName();
    }

    public WaitingList withId(final long waitingListId) {
        return new WaitingList(waitingListId, name, reservationDate, theme, reservationTime, createdAt);
    }
}