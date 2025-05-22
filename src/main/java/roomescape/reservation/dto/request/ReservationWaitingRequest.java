package roomescape.reservation.dto.request;

import java.time.LocalDate;

public record ReservationWaitingRequest(LocalDate date, Long time, Long theme) {
    public ReservationWaitingRequest {
        if (date == null) {
            throw new IllegalArgumentException("날짜는 null 일 수 없습니다.");
        }

        if (time == null) {
            throw new IllegalArgumentException("예약 시간 번호는 null 일 수 없습니다.");
        }

        if (theme == null) {
            throw new IllegalArgumentException("테마 번호는 null 일 수 없습니다.");
        }
    }
}
