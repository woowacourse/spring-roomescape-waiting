package roomescape.dto.request;

import java.time.LocalDate;

public record ReservationAdminRegisterDto(
        LocalDate date,
        Long themeId,
        Long timeId,
        Long memberId
) {
    public ReservationAdminRegisterDto {
        if (date == null) {
            throw new IllegalArgumentException("예약 날짜는 null 일 수 없습니다.");
        }

        if (themeId == null) {
            throw new IllegalArgumentException("테마의 id 는 null 일 수 없습니다.");
        }

        if (timeId == null) {
            throw new IllegalArgumentException("예약 시각의 id 는 null 일 수 없습니다.");
        }

        if (memberId == null) {
            throw new IllegalArgumentException("회원의 id 는 null 일 수 없습니다.");
        }
    }
}
