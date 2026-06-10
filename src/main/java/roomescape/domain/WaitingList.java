package roomescape.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class WaitingList {

    private final Long id;
    private final PersonName name;
    private final ReservationDate reservationDate;
    private final ReservationTime reservationTime;
    private final Theme theme;
    private final LocalDateTime createdAt;

    private static void validateId(final Long id) {
        if (id == null) {
            throw new IllegalArgumentException("예약 대기 ID는 비워둘 수 없습니다.");
        }
    }

    public static WaitingList create(final String name, final LocalDate date, final ReservationTime reservationTime, final Theme theme) {
        return new WaitingList(null, new PersonName(name), new ReservationDate(date), reservationTime, theme, LocalDateTime.now());
    }

    public static WaitingList createWithId(final Long id, final String name, final LocalDate date, final ReservationTime reservationTime, final Theme theme, final LocalDateTime createdAt) {
        validateId(id);
        return new WaitingList(id, new PersonName(name), new ReservationDate(date), reservationTime, theme, createdAt);
    }

    public WaitingList withId(final Long waitingListId) {
        validateId(waitingListId);
        return new WaitingList(waitingListId, name, reservationDate, reservationTime, theme, createdAt);
    }

    public boolean isOwner(final String name) {
        return this.name.isSameName(name);
    }

    public String getName() {
        return name.name();
    }
}
