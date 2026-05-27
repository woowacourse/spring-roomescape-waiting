package roomescape.domain;

import java.time.LocalDateTime;

import roomescape.exception.CustomException;
import roomescape.exception.ErrorCode;

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
            throw new CustomException(ErrorCode.RESERVATION_NAME_BLANK);
        }

        if (name.length() > 255) {
            throw new CustomException(ErrorCode.RESERVATION_NAME_TOO_LONG);
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
