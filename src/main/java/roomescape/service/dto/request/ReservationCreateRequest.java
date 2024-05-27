package roomescape.service.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record ReservationCreateRequest(
        @NotNull
        Long memberId,
        @NotNull(message = "예약 날짜를 입력해주세요.")
        LocalDate date,
        @NotNull
        Long timeId,
        @NotNull
        Long themeId
) {

    public static ReservationCreateRequest from(ReservationCreateMemberRequest adminRequest, Long memberId) {
        return new ReservationCreateRequest(memberId, adminRequest.date(), adminRequest.timeId(),
                adminRequest.themeId());
    }
}
