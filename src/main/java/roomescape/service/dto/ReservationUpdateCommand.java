package roomescape.service.dto;

import java.time.LocalDate;
import roomescape.exception.client.InvalidCommandException;

public class ReservationUpdateCommand {
    private final Long id;
    private final String name;
    private final LocalDate date;
    private final Long timeId;

    public ReservationUpdateCommand(Long id, String name, LocalDate date, Long timeId) {
        validate(id, name, date, timeId);
        this.id = id;
        this.name = name;
        this.date = date;
        this.timeId = timeId;
    }

    private void validate(Long id, String name, LocalDate date, Long timeId) {
        if (id == null) {
            throw new InvalidCommandException("예약 ID는 비어 있을 수 없습니다.");
        }
        if (name == null || name.isBlank()) {
            throw new InvalidCommandException("예약자 이름은 비어 있을 수 없습니다.");
        }
        if (date == null) {
            throw new InvalidCommandException("예약 날짜는 비어 있을 수 없습니다.");
        }
        if (timeId == null) {
            throw new InvalidCommandException("예약 시간을 선택해 주세요.");
        }
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

    public Long getTimeId() {
        return timeId;
    }
}
