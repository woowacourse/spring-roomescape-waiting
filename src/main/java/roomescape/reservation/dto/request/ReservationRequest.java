package roomescape.reservation.dto.request;

import java.time.LocalDate;

public record ReservationRequest(LocalDate date, Long timeId, Long themeId) {
    public ReservationRequest {
        if (date == null) {
            throw new IllegalArgumentException("날짜는 null 일 수 없습니다.");
        }

        if (timeId == null) {
            throw new IllegalArgumentException("예약 시간 번호는 null 일 수 없습니다.");
        }

        if (themeId == null) {
            throw new IllegalArgumentException("테마 번호는 null 일 수 없습니다.");
        }
    }
}
