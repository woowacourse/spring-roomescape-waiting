package roomescape.reservation.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record AdminReservationCreateRequest(
    @JsonFormat(pattern = "yyyy-MM-dd") @NotNull(message = "예약할 날짜가 입력되지 않았다.") LocalDate date,
    @NotNull(message = "예약하는 멤버가 누구인지 입력되지 않았다.") Long memberId,
    @NotNull(message = "예약할 시간이 입력되지 않았다.")Long timeId,
    @NotNull(message = "예약할 테마가 입력되지 않았다.")Long themeId) {

}
