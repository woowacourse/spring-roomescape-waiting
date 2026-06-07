package roomescape.domain;

import lombok.Getter;
import roomescape.exception.BusinessException;
import roomescape.exception.ErrorCode;

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

    public WaitingList withId(final long waitingListId) {
        return new WaitingList(waitingListId, name, reservationDate, theme, reservationTime, createdAt);
    }

    public void validateForDeletion(final String name) {
        validateOwner(name);
        validateNotPast();
    }

    public void validateNotPast() {
        if (reservationDate.isPast()) {
            throw new BusinessException(ErrorCode.DATE_ALREADY_PASSED);
        }
        if (reservationDate.isToday() && reservationTime.isBefore()) {
            throw new BusinessException(ErrorCode.TIME_ALREADY_PASSED);
        }
    }

    public void validateOwner(final String name) {
        if (!this.name.getName().equals(name)) {
            throw new BusinessException(ErrorCode.USER_NAME_NOT_MATCHED);
        }
    }

    public String getName() {
        return name.getName();
    }
}
