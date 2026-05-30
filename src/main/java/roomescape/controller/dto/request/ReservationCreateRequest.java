package roomescape.controller.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

public class ReservationCreateRequest {
    @NotNull(message = "이름은 필수로 입력해야 합니다")
    private final String name;

    @NotNull(message = "날짜는 필수로 입력해야 합니다")
    private final LocalDate date;

    @NotNull(message = "Time ID는 필수로 입력해야 합니다.")
    @Positive(message = "Time ID는 양수여야 합니다.")
    private final Long timeId;

    @NotNull(message = "Theme ID는 필수로 입력해야 합니다.")
    @Positive(message = "Theme ID는 양수여야 합니다.")
    private final Long themeId;

    public ReservationCreateRequest(String name, LocalDate date, Long timeId, Long themeId) {
        this.name = name;
        this.date = date;
        this.timeId = timeId;
        this.themeId = themeId;
    }

    public String getName() {
        return name;
    }

    public LocalDate getDate() {
        return date;
    }

    public Long getTimeId() {
        return timeId;
    }

    public Long getThemeId() {
        return themeId;
    }
}
