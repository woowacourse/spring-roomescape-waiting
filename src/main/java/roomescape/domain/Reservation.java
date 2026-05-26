package roomescape.domain;

import roomescape.domain.exception.DomainConflictException;
import roomescape.domain.exception.DomainRuleViolationException;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Reservation {

    private final Long id;
    private final String name;
    private final LocalDate date;
    private final ReservationTime time;
    private final Theme theme;

    public Reservation(Long id, String name, LocalDate date, ReservationTime time, Theme theme) {
        if (name == null || name.isBlank()) {
            throw new DomainRuleViolationException("예약자 이름은 비어 있을 수 없습니다.");
        }
        if (date == null) {
            throw new DomainRuleViolationException("예약 날짜는 비어 있을 수 없습니다.");
        }
        if (time == null) {
            throw new DomainRuleViolationException("예약 시간은 비어 있을 수 없습니다.");
        }
        if (theme == null) {
            throw new DomainRuleViolationException("예약 테마는 비어 있을 수 없습니다.");
        }
        this.id = id;
        this.name = name;
        this.date = date;
        this.time = time;
        this.theme = theme;
    }

    private Reservation(String name, LocalDate date, ReservationTime time, Theme theme) {
        this(null, name, date, time, theme);
    }

    public static Reservation create(String name, LocalDate date, ReservationTime time, Theme theme, LocalDateTime now) {
        if (time.isPast(date, now)) {
            throw new DomainConflictException("지난 시간으로는 예약할 수 없습니다.");
        }
        return new Reservation(name, date, time, theme);
    }

    public Reservation changeSchedule(LocalDate newDate, ReservationTime newTime, String requester, LocalDateTime now) {
        validateOwner(requester);
        if (isPast(now)) {
            throw new DomainConflictException("지난 예약은 변경할 수 없습니다.");
        }
        if (newTime.isPast(newDate, now)) {
            throw new DomainConflictException("과거로는 변경할 수 없습니다.");
        }
        return new Reservation(id, name, newDate, newTime, theme);
    }

    public void checkCancellable(String requester, LocalDateTime now) {
        validateOwner(requester);
        if (isPast(now)) {
            throw new DomainConflictException("지난 예약은 취소할 수 없습니다.");
        }
    }

    private void validateOwner(String name) {
        if (!this.name.equals(name)) {
            throw new DomainConflictException("본인의 예약만 수정할 수 있습니다.");
        }
    }

    private boolean isPast(LocalDateTime now) {
        return LocalDateTime.of(date, time.getStartAt()).isBefore(now);
    }

    public boolean isSameReservation(Reservation other) {
        return other != null && id != null && id.equals(other.id);
    }

    public Long getId() {
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

    public Theme getTheme() {
        return theme;
    }
}
