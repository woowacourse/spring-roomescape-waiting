package roomescape.service.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record UserReservationRequest(
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        @NotNull(message = "날짜를 입력해주세요.")
        LocalDate date,
        @NotNull(message = "시간 ID를 입력해주세요.")
        Long timeId,
        @NotNull(message = "테마 ID를 입력해주세요.")
        Long themeId) {

    public ReservationRequest toReservationRequest(Long memberId) {
        return new ReservationRequest(date, timeId, themeId, memberId);
    }
}
