package roomescape.controller.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class AvailableTimeFindRequest {
    @NotNull(message = "날짜는 필수로 입력해야 합니다.")
    private final LocalDate date;

    @NotNull(message = "Theme ID는 필수로 입력해야 합니다.")
    @Positive(message = "Theme ID는 양수여야 합니다.")
    private final Long themeId;
}
