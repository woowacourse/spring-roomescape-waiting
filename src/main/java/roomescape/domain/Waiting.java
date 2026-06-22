package roomescape.domain;

import roomescape.domain.exception.DomainConflictException;
import roomescape.domain.exception.DomainRuleViolationException;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Waiting {

    private final Long id;
    private final String name;
    private final LocalDate date;
    private final ReservationTime time;
    private final Theme theme;

    public Waiting(Long id, String name, LocalDate date, ReservationTime time, Theme theme) {
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

    private Waiting(String name, LocalDate date, ReservationTime time, Theme theme) {
        this(null, name, date, time, theme);
    }

    public static Waiting create(String name, LocalDate date, ReservationTime time, Theme theme, LocalDateTime now) {
        if (time.isPast(date, now)) {
            throw new DomainConflictException("지난 시간으로는 예약할 수 없습니다.");
        }
        return new Waiting(name, date, time, theme);
    }

    public void validateOwner(String name) {
        if (!this.name.equals(name)) {
            throw new DomainConflictException("본인의 예약대기만 취소할 수 있습니다.");
        }
    }

    public Reservation promote(LocalDateTime now) {
        return Reservation.promote(this.name, this.date, this.time, this.theme, now);
    }

    public boolean isSameName(String name) {
        return this.name.equals(name);
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
