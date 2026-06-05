package roomescape.service.dto;

import java.time.LocalDate;
import roomescape.exception.client.InvalidCommandException;

public class ReservationCreateCommand {
    private final String name;
    private final LocalDate date;
    private final Long timeId;

    private final Long themeId;

    public ReservationCreateCommand(String name, LocalDate date, Long timeId, Long themeId) {
        validate(name, date, timeId, themeId);
        this.name = name;
        this.date = date;
        this.timeId = timeId;
        this.themeId = themeId;
    }

    private void validate(String name, LocalDate date, Long timeId, Long themeId) {
        if (name == null || name.isBlank()) {
            throw new InvalidCommandException("예약자 이름은 비어 있을 수 없습니다.");
        }
        if (date == null) {
            throw new InvalidCommandException("예약 날짜는 비어 있을 수 없습니다.");
        }
        if (timeId == null) {
            throw new InvalidCommandException("예약 시간을 선택해 주세요.");
        }
        if (themeId == null) {
            throw new InvalidCommandException("예약 테마를 선택해 주세요.");
        }
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

    public Long getThemeId() {
        return themeId;
    }
}
