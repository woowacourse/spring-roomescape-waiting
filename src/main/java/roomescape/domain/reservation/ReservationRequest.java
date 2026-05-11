package roomescape.domain.reservation;

import java.time.LocalDate;

public record ReservationRequest(String name, LocalDate date, Long timeId, Long themeId) {

    public ReservationRequest {
        validateName(name);
        validateDate(date);
        validateTimeId(timeId);
        validateThemeId(themeId);
    }

    private static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("이름은 필수입니다.");
        }
    }

    private static void validateDate(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("날짜는 필수입니다.");
        }
    }

    private static void validateTimeId(Long timeId) {
        if (timeId == null) {
            throw new IllegalArgumentException("시간은 필수입니다.");
        }
    }

    private static void validateThemeId(Long themeId) {
        if (themeId == null) {
            throw new IllegalArgumentException("테마는 필수입니다.");
        }
    }
}
