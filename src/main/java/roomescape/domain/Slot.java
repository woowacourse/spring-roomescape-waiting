package roomescape.domain;

import roomescape.exception.ErrorType;
import roomescape.exception.RoomescapeException;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Slot {

    private final Long id;
    private final LocalDate date;
    private final Theme theme;
    private final ReservationTime time;
    private final Store store;

    public Slot(Long id, LocalDate date, Theme theme, ReservationTime time, Store store) {
        validate(date, theme, time, store);
        this.id = id;
        this.date = date;
        this.theme = theme;
        this.time = time;
        this.store = store;
    }

    private void validate(LocalDate date, Theme theme, ReservationTime time, Store store) {
        if (theme == null) {
            throw new RoomescapeException(ErrorType.INVALID_DOMAIN, "테마는 필수입니다.");
        }
        if (date == null) {
            throw new RoomescapeException(ErrorType.INVALID_DOMAIN, "예약 날짜는 필수입니다.");
        }
        if (time == null) {
            throw new RoomescapeException(ErrorType.INVALID_DOMAIN, "예약 시간은 필수입니다.");
        }
        if (store == null) {
            throw new RoomescapeException(ErrorType.INVALID_DOMAIN, "매장은 필수입니다.");
        }
    }

    public boolean isInPast(LocalDateTime currentDateTime) {
        LocalDateTime slotDateTime = LocalDateTime.of(date, time.getStartAt());
        return slotDateTime.isBefore(currentDateTime);
    }

    public boolean hasSameSlot(Slot other) {
        return date.equals(other.date) && time.getId().equals(other.time.getId()) && theme.getId()
                .equals(other.theme.getId());
    }

    public Slot withId(Long id) {
        return new Slot(id, date, theme, time, store);
    }

    public Long getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public Theme getTheme() {
        return theme;
    }

    public ReservationTime getTime() {
        return time;
    }

    public Store getStore() {
        return store;
    }
}