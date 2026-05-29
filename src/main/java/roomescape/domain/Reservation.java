package roomescape.domain;

import java.time.LocalDateTime;

import roomescape.domain.exception.DomainErrorCode;
import roomescape.domain.exception.RoomescapeException;

public class Reservation {

    private final Long id;
    private final String name;
    private final Long scheduleId;
    private final Status status;
    private final LocalDateTime updateAt;

    public Reservation(Long id, String name, Long scheduleId, Status status, LocalDateTime updateAt) {
        validateName(name);

        this.id = id;
        this.name = name;
        this.scheduleId = scheduleId;
        this.status = status;
        this.updateAt = updateAt;
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new RoomescapeException(DomainErrorCode.INVALID_INPUT, "예약자 이름은 비거나 공백일 수 없습니다.");
        }

        if (name.length() > 255) {
            throw new RoomescapeException(DomainErrorCode.INVALID_INPUT, "예약자 이름은 255자를 초과할 수 없습니다.");
        }
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Long getScheduleId() {
        return scheduleId;
    }

    public Status getStatus() {
        return status;
    }

    public LocalDateTime getUpdateAt() {
        return updateAt;
    }
}
