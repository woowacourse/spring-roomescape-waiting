package roomescape.reservation.time.presentation.dto;

import java.time.LocalDate;

public record TimeConditionRequest(LocalDate date, Long themeId) {
    public TimeConditionRequest {
        if (date == null) {
            throw new IllegalArgumentException("date는 null 일 수 없습니다.");
        }

        if (themeId == null) {
            throw new IllegalArgumentException("theme id는 null 일 수 없습니다.");
        }
    }
}
