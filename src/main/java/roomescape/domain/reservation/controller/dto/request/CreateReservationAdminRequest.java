package roomescape.domain.reservation.controller.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import roomescape.domain.reservation.application.dto.request.CreateReservationServiceRequest;

public record CreateReservationAdminRequest(
        @NotNull(message = "예약자를 필수로 입력해야 합니다.")
        Long memberId,
        @NotNull(message = "날짜를 필수로 입력해야 합니다.")
        LocalDate date,
        @NotNull(message = "시간을 필수로 입력해야 합니다.")
        Long timeId,
        @NotNull(message = "테마를 필수로 입력해야 합니다.")
        Long themeId,
        @NotNull(message = "사용자 이름을 필수로 입력해야 합니다.")
        String memberName
) {

    public CreateReservationServiceRequest toServiceRequest() {
        return new CreateReservationServiceRequest(memberName, memberId, date, timeId, themeId);
    }
}
