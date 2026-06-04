package roomescape.domain;

import roomescape.domain.exception.DomainConflictException;
import roomescape.domain.exception.DomainRuleViolationException;

import java.time.LocalDateTime;

public class Waiting {

    private final Long id;
    private final String name;
    private final Schedule schedule;

    public Waiting(Long id, String name, Schedule schedule) {
        if (name == null || name.isBlank()) {
            throw new DomainRuleViolationException("예약자 이름은 비어 있을 수 없습니다.");
        }
        this.id = id;
        this.name = name;
        this.schedule = schedule;
    }

    private Waiting(String name, Schedule schedule) {
        this(null, name, schedule);
    }

    public static Waiting create(String name, Schedule schedule, LocalDateTime now) {
        if (schedule.isPast(now)) {
            throw new DomainConflictException("지난 시간으로는 예약할 수 없습니다.");
        }
        return new Waiting(name, schedule);
    }

    public void validateCancelableBy(String name) {
        if (!this.name.equals(name)) {
            throw new DomainConflictException("본인의 예약대기만 취소할 수 있습니다.");
        }
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

    public Schedule getSchedule() {
        return schedule;
    }
}
