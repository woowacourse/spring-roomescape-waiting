package roomescape.reservation.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import roomescape.member.domain.Member;

public record ReservationRequest(
        @NotNull(message = "날짜가 존재하지 않습니다.") LocalDate date,
        @NotNull(message = "예약자 정보가 입력되지 않았습니다.") Member member,
        long timeId,
        long themeId
) {

}
