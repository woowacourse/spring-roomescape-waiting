package roomescape.domain;

import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
public class WaitingList {

    private final Long id;

    private final PersonName name;
    private final LocalDate date;

    private final Theme theme;
    private final ReservationTime reservationTime;

    private final LocalDateTime createdAt;

    private WaitingList(Long id, PersonName name, LocalDate date, Theme theme, ReservationTime reservationTime, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.theme = theme;
        this.reservationTime = reservationTime;
        this.createdAt = createdAt;
    }

    public static WaitingList create(LocalDate date, String name, Theme theme, ReservationTime reservationTime) {
        return new WaitingList(null, new PersonName(name), date, theme, reservationTime, LocalDateTime.now());
    }

    public String getName() {
        return name.getName();
    }

    public WaitingList withId(long waitingListId) {
        return new WaitingList(waitingListId, name, date, theme, reservationTime, createdAt);
    }
}
