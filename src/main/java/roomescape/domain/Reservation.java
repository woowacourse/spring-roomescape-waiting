package roomescape.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import roomescape.exception.InvalidDomainException;

public class Reservation {

    private final Long id;
    private final User user;
    private final Theme theme;
    private final LocalDate date;
    private final ReservationTime time;
    private final Store store;

    public Reservation(Long id, User user, Theme theme, LocalDate date, ReservationTime time, Store store) {
        validate(user, theme, date, time, store);
        this.id = id;
        this.user = user;
        this.theme = theme;
        this.date = date;
        this.time = time;
        this.store = store;
    }

    private void validate(User user, Theme theme, LocalDate date, ReservationTime time, Store store) {
        if (user == null) {
            throw new InvalidDomainException("예약자는 필수입니다.");
        }
        if (theme == null) {
            throw new InvalidDomainException("테마는 필수입니다.");
        }
        if (date == null) {
            throw new InvalidDomainException("예약 날짜는 필수입니다.");
        }
        if (time == null) {
            throw new InvalidDomainException("예약 시간은 필수입니다.");
        }
        if (store == null) {
            throw new InvalidDomainException("매장은 필수입니다.");
        }
    }

    public boolean isInPast(LocalDateTime currentDateTime) {
        LocalDateTime reservationDateTime = LocalDateTime.of(date, time.getStartAt());
        return reservationDateTime.isBefore(currentDateTime);
    }

    public boolean hasSameSlot(Reservation other) {
        return date.equals(other.date)
                && time.getId().equals(other.time.getId())
                && theme.getId().equals(other.theme.getId());
    }

    public Reservation withId(Long id) {
        return new Reservation(id, user, theme, date, time, store);
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Theme getTheme() {
        return theme;
    }

    public LocalDate getDate() {
        return date;
    }

    public ReservationTime getTime() {
        return time;
    }

    public Store getStore() {
        return store;
    }
}
