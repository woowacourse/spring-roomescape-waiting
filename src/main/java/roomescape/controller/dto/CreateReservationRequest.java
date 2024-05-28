package roomescape.controller.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

public record CreateReservationRequest(
    @NotNull(message = "null일 수 없습니다.")
    @Positive
    Long memberId,

    @NotNull(message = "null일 수 없습니다.")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @FutureOrPresent(message = "과거 날짜로는 예약할 수 없습니다.")
    LocalDate date,

    @NotNull(message = "null일 수 없습니다.")
    @Positive(message = "양수만 입력할 수 있습니다.")
    Long timeId,

    @NotNull(message = "null일 수 없습니다.")
    @Positive(message = "양수만 입력할 수 있습니다.")
    Long themeId
) { }
