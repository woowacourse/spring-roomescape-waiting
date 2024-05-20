package roomescape.reservation.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

public record AdminReservationAddRequest(
        @NotNull(message = "예약 날짜는 필수 입니다.") LocalDate date,
        @NotNull(message = "멤버 아이디는 필수 입니다.") @Positive Long memberId,
        @NotNull(message = "예약 시간 선택은 필수 입니다.") @Positive Long timeId,
        @NotNull(message = "테마 선택은 필수 입니다.") @Positive Long themeId) {

    public MemberReservationAddRequest toMemberRequest() {
        return new MemberReservationAddRequest(date, timeId, themeId);
    }
}
