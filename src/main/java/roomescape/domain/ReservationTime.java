package roomescape.domain;

import roomescape.domain.exception.DomainRuleViolationException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class ReservationTime {

    private final Long id;
    private final LocalTime startAt;

    public ReservationTime(Long id, LocalTime startAt) {
        if (startAt == null) {
            throw new DomainRuleViolationException("예약 시작 시간은 비어 있을 수 없습니다.");
        }
        this.id = id;
        this.startAt = startAt;
    }

    public ReservationTime(LocalTime startAt) {
        this(null, startAt);
    }

    public boolean isPast(LocalDate date, LocalDateTime now) {
        return LocalDateTime.of(date, startAt).isBefore(now);
    }

    public Long getId() {
        return id;
    }

    public LocalTime getStartAt() {
        return startAt;
    }
}
