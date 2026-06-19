package roomescape.domain;

import roomescape.domain.exception.DomainConflictException;
import roomescape.domain.exception.DomainRuleViolationException;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Reservation {

    private final Long id;
    private final String name;
    private final Schedule schedule;
    private final ReservationStatus status;

    public Reservation(Long id, String name, Schedule schedule) {
        this(id, name, schedule, ReservationStatus.CONFIRMED);
    }

    public Reservation(Long id, String name, Schedule schedule, ReservationStatus status) {
        if (name == null || name.isBlank()) {
            throw new DomainRuleViolationException("예약자 이름은 비어 있을 수 없습니다.");
        }
        this.id = id;
        this.name = name;
        this.schedule = schedule;
        this.status = status;
    }

    private Reservation(String name, Schedule schedule, ReservationStatus status) {
        this(null, name, schedule, status);
    }

    public static Reservation create(String name, Schedule schedule, LocalDateTime now) {
        if(schedule.isPast(now)) {
            throw new DomainConflictException("지난 시간으로는 예약할 수 없습니다.");
        }
        return new Reservation(name, schedule, ReservationStatus.PENDING);
    }

    public Reservation confirm() {
        return new Reservation(id, name, schedule, ReservationStatus.CONFIRMED);
    }

    public Reservation changeSchedule(LocalDate newDate, ReservationTime newTime, String requester, LocalDateTime now) {
        validateOwner(requester);
        if (isPast(now)) {
            throw new DomainConflictException("지난 예약은 변경할 수 없습니다.");
        }
        if (newTime.isPast(newDate, now)) {
            throw new DomainConflictException("과거로는 변경할 수 없습니다.");
        }
        return new Reservation(id, name, new Schedule(newDate, newTime, schedule.getTheme()), status);
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
        return schedule.isPast(now);
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

    public Schedule getSchedule() {
        return schedule;
    }

    public ReservationStatus getStatus() {
        return status;
    }
}
