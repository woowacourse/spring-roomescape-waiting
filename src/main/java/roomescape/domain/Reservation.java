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
    private final ReservationStatus status;

    public Reservation(Long id, User user, Theme theme, LocalDate date, ReservationTime time, Store store,
                       ReservationStatus status) {
        validate(user, theme, date, time, store, status);
        this.id = id;
        this.user = user;
        this.theme = theme;
        this.date = date;
        this.time = time;
        this.store = store;
        this.status = status;
    }

    private void validate(User user, Theme theme, LocalDate date, ReservationTime time, Store store,
                          ReservationStatus status) {
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
        if (status == null) {
            throw new InvalidDomainException("예약 상태는 필수입니다.");
        }
    }

    public boolean isInPast(LocalDateTime currentDateTime) {
        LocalDateTime reservationDateTime = LocalDateTime.of(date, time.getStartAt());
        return reservationDateTime.isBefore(currentDateTime);
    }

    public boolean hasSameSlot(Reservation other) {
        return date.equals(other.date) && time.getId().equals(other.time.getId()) && theme.getId()
                .equals(other.theme.getId());
    }

    public boolean isReserved() {
        return status.equals(ReservationStatus.RESERVED);
    }

    public boolean isWaiting() {
        return status.equals(ReservationStatus.WAITING);
    }

    public Reservation withId(Long id) {
        if (this.id != null) {
            throw new InvalidDomainException("이미 id가 존재하는 도메인입니다. 도메인 id는 생성 이후 수정될 수 없습니다.");
        }
        return new Reservation(id, user, theme, date, time, store, status);
    }

    public Reservation withStatus(ReservationStatus status) {
        return new Reservation(id, user, theme, date, time, store, status);
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

    public ReservationStatus getStatus() {
        return status;
    }
}
